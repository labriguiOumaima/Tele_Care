#!/usr/bin/env python3
"""Build, start, exercise and stop Tele-Care. Python stdlib only; Windows/Linux."""
import argparse, base64, concurrent.futures, json, os, pathlib, shutil, socket, subprocess, sys, time, urllib.request, urllib.error, uuid
ROOT=pathlib.Path(__file__).resolve().parents[1]
RUN=ROOT/'.runtime'; REPORT=ROOT/'reports'
MODULES=['config-server','eureka-server','service-beneficiaires','service-utilisateurs','service-alertes','service-interventions','service-notification','api-gateway']
PORTS=dict(zip(MODULES,[8888,8761,8081,8084,8082,8083,8085,8080]))
PROCS={}; RESULTS=[]; RECORDS=[]; KEEP=False
opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
class Failure(Exception):pass
class StartupFailure(Failure):fatal=True

def request(method,url,data=None,key=None,auth=False,expected=200,timeout=20):
    h={'Accept':'application/json'}
    if data is not None:h['Content-Type']='application/json'
    if key:h['Idempotency-Key']=key
    if auth:
        user=os.environ.get('SERVICE_UTILISATEURS_USERNAME','admin');password=os.environ.get('SERVICE_UTILISATEURS_PASSWORD','admin123')
        h['Authorization']='Basic '+base64.b64encode((user+':'+password).encode()).decode()
    raw=None if data is None else json.dumps(data,ensure_ascii=False,separators=(',',':')).encode()
    req=urllib.request.Request(url,raw,h,method=method)
    try:
        with opener.open(req,timeout=timeout) as res:status=res.status;body=res.read();headers=dict(res.headers)
    except urllib.error.HTTPError as e:status=e.code;body=e.read();headers=dict(e.headers)
    except Exception as e:raise Failure(f'{method} {url}: {e}') from e
    try:payload=json.loads(body) if body else None
    except ValueError:payload=body.decode(errors='replace')
    if expected is not None and status not in ([expected] if isinstance(expected,int) else expected):raise Failure(f'{method} {url}: attendu {expected}, obtenu {status}: {payload}')
    return payload,headers,status

def api(port,path,method='GET',data=None,key=None,auth=False,expected=200):
    result=request(method,f'http://localhost:{port}{path}',data,key,auth,expected)
    # Keep the actual HTTP exchanges as evidence, with credentials excluded.
    RECORDS.append({'port':port,'method':method,'path':path,'request':data,'key':key,'auth':auth,'expected':result[2],'response':result[0]})
    return result[0]

def eventually(fn,seconds=90):
    end=time.monotonic()+seconds;last=None
    while time.monotonic()<end:
        try:
            value=fn()
            if value:return value
        except Exception as e:
            if getattr(e,"fatal",False):raise
            last=e
        time.sleep(1)
    raise Failure(f'Delai depasse ({seconds}s). Derniere erreur: {last}')

def check(label,fn):
    start=time.monotonic()
    try:fn();ok=True;detail='OK'
    except Exception as e:ok=False;detail=str(e)
    RESULTS.append({'test':label,'passed':ok,'detail':detail,'seconds':round(time.monotonic()-start,2)})
    print(('OK  ' if ok else 'ECHEC ')+label+('' if ok else ': '+detail),flush=True)
    if not ok:raise Failure(detail)

def tcp(port):
    try:
        with socket.create_connection(('127.0.0.1',port),timeout=.3):return True
    except OSError:return False

def start_module(mod,extra=None):
    jars=[p for p in (ROOT/mod/'target').glob('*.jar') if not p.name.endswith(('-sources.jar','-tests.jar'))]
    if len(jars)!=1:raise Failure(f'JAR introuvable/ambigu pour {mod}; lancer la compilation.')
    log=open(RUN/(mod+'.log'),'ab',buffering=0)
    args=['java','-Xms32m','-Xmx256m','-XX:MaxMetaspaceSize=160m','-jar',str(jars[0])]
    if mod=='config-server':args+=['--spring.profiles.active=native','--spring.cloud.config.server.native.search-locations='+(ROOT/'config-repo').as_uri()+'/']
    elif mod.startswith('service-'):
        db=(RUN/'data'/mod).resolve().as_posix()
        args+=['--spring.datasource.url=jdbc:h2:file:'+db]
    args+=(extra or [])
    p=subprocess.Popen(args,cwd=ROOT/mod,stdout=log,stderr=subprocess.STDOUT)
    PROCS[mod]=p
    (RUN/'pids.json').write_text(json.dumps({k:v.pid for k,v in PROCS.items()}))
    url=f'http://localhost:{PORTS[mod]}/actuator/health'
    def healthy():
        if p.poll() is not None:raise StartupFailure(f'{mod} arrete. Voir {RUN/(mod+".log")}')
        return request('GET',url,timeout=2)[0].get('status')=='UP'
    eventually(healthy,120)
    print('Demarre: '+mod,flush=True)

def stop_module(mod):
    p=PROCS.pop(mod,None)
    if p is not None:
        p.terminate()
        try:p.wait(timeout=20)
        except subprocess.TimeoutExpired:p.kill();p.wait(timeout=10)

def stop_owned():
    for mod in list(PROCS)[::-1]:stop_module(mod)
    if (RUN/'pids.json').exists():(RUN/'pids.json').unlink()

def export_postman():
    items=[]
    for i,r in enumerate(RECORDS,1):
        req={'method':r['method'],'url':f'http://localhost:{r["port"]}{r["path"]}','header':[]}
        if r['request'] is not None:
            req['header'].append({'key':'Content-Type','value':'application/json'})
            req['body']={'mode':'raw','raw':json.dumps(r['request'],ensure_ascii=False),'options':{'raw':{'language':'json'}}}
        if r['key']:req['header'].append({'key':'Idempotency-Key','value':r['key']})
        if r['auth']:req['auth']={'type':'basic','basic':[{'key':'username','value':'{{username}}','type':'string'},{'key':'password','value':'{{password}}','type':'string'}]}
        item={'name':f'{i:03d} {r["method"]} {r["path"]}','request':req,'event':[{'listen':'test','script':{'type':'text/javascript','exec':[f'pm.test("HTTP {r["expected"]}", function () {{ pm.response.to.have.status({r["expected"]}); }});']}}],
              'response':[{'name':'Resultat observe','status':'Observed','code':r['expected'],'header':[{'key':'Content-Type','value':'application/json'}],'body':json.dumps(r['response'],ensure_ascii=False)}]}
        items.append(item)
    collection={'info':{'name':'Tele-Care - traces du test complet (IDs du dernier run)','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json','description':'Requetes et reponses reelles du dernier run. Les IDs sont ceux de la base de ce run; relancer le script pour reconstruire un scenario independant. Les arrets/redemarrages et attentes Kafka sont effectues par le script, pas par cette collection.'},'variable':[{'key':'username','value':'admin'},{'key':'password','value':'admin123'}],'item':items}
    (REPORT/'Tele-Care-observed.postman_collection.json').write_text(json.dumps(collection,ensure_ascii=False,indent=2))

def scenarios(with_faults=True):
    run=uuid.uuid4().hex[:10]
    def key(s):return run+'-'+s
    context={}
    def config_check():
        for m in MODULES:
            if m in ['eureka-server','config-server']:continue
            cfg=api(8888,'/'+m+'/default')
            assert any(m+'.properties' in p['name'] for p in cfg['propertySources']),m
    check('V3 - configurations des cinq services et de la Gateway',config_check)
    def registry():
        def all_up():
            apps=api(8761,'/eureka/apps')['applications']['application']
            if isinstance(apps,dict):apps=[apps]
            names={a['name'] for a in apps if any(i['status']=='UP' for i in (a['instance'] if isinstance(a['instance'],list) else [a['instance']]))}
            return all(n.upper() in names for n in MODULES if n not in ['config-server','eureka-server'])
        eventually(all_up,90)
    check('V2 - Eureka : cinq services metier et Gateway UP',registry)
    def users():
        body={'nom':'Assistant '+run,'email':run+'@telecare.test','role':'ASSISTANT'}
        u=api(8084,'/api/utilisateurs','POST',body,key('user'),True,201);context['assistant']=u['id']
        u2=api(8084,'/api/utilisateurs','POST',body,key('user'),True,201);assert u2==u
        api(8084,'/api/utilisateurs','POST',dict(body,nom='Change'),key('user'),True,409)
        api(8084,'/api/utilisateurs','POST',body,None,True,409)
        assert api(8084,f'/api/utilisateurs/{u["id"]}')['actif']
        assert api(8084,'/api/utilisateurs',auth=True)['totalElements']>=1
        api(8084,f'/api/utilisateurs/{u["id"]}','PUT',dict(body,nom='Assistant modifie'),auth=True)
        api(8084,'/api/utilisateurs/99999999',expected=404)
        api(8084,'/api/utilisateurs','POST',{'nom':'','email':'bad','role':''},auth=True,expected=400)
        api(8084,'/api/utilisateurs','POST',body,expected=401)
        api(8084,'/api/utilisateurs?page=-1',auth=True,expected=400)
        # A temporary non-assistant exercises full delete CRUD without breaking the cycle.
        tmp=api(8084,'/api/utilisateurs','POST',dict(body,email='tmp-'+body['email'],role='AUTRE'),auth=True,expected=201)
        api(8084,f'/api/utilisateurs/{tmp["id"]}','DELETE',auth=True,expected=204)
        api(8084,f'/api/utilisateurs/{tmp["id"]}',expected=404)
    check('V1/V6 - utilisateurs CRUD, validation, Basic, replay et conflit',users)
    def beneficiaries():
        body={'nom':'Beneficiaire '+run,'age':78,'adresse':'Casablanca','pathologie':'Test','telephone':'0600000000'}
        b=api(8081,'/beneficiaires','POST',body,key('b'),expected=201);context['beneficiary']=b['id']
        assert api(8081,'/beneficiaires','POST',body,key('b'),expected=201)==b
        api(8081,'/beneficiaires','POST',dict(body,age=79),key('b'),expected=409)
        assert api(8081,f'/beneficiaires/{b["id"]}')['nom']==body['nom']
        api(8081,f'/beneficiaires/{b["id"]}','PUT',dict(body,nom='Beneficiaire modifie'))
        api(8081,'/beneficiaires','POST',dict(body,age=-1),key('bad'),expected=400)
        api(8081,'/beneficiaires/99999999',expected=404)
        assert api(8081,f'/beneficiaires/{b["id"]}/contacts')==[]
        cbody={'beneficiaryId':b['id'],'nom':'Contact test','lien':'Famille','telephone':'0600000001','email':run+'@example.test'}
        c=api(8081,'/contacts','POST',cbody,key('contact'),expected=201);context['contact']=c['id']
        assert api(8081,'/contacts','POST',cbody,key('contact'),expected=201)==c
        api(8081,'/contacts','POST',dict(cbody,email='invalide'),key('bad-email'),expected=400)
        api(8081,'/contacts','POST',dict(cbody,beneficiaryId=99999999),key('bad-ref'),expected=404)
        assert api(8081,f'/contacts/{c["id"]}')['email']==cbody['email']
        api(8081,f'/contacts/{c["id"]}','PUT',dict(cbody,nom='Contact modifie'))
        assert len(api(8081,f'/beneficiaires/{b["id"]}/dossier')['contacts'])==1
        api(8081,f'/beneficiaires/{b["id"]}','DELETE',expected=409)
        tmp=api(8081,'/beneficiaires','POST',body,key('tmp-b'),expected=201)
        tc=api(8081,'/contacts','POST',dict(cbody,beneficiaryId=tmp['id']),key('tmp-c'),expected=201)
        api(8081,f'/contacts/{tc["id"]}','DELETE',expected=204)
        api(8081,f'/contacts/{tc["id"]}',expected=404)
        api(8081,f'/beneficiaires/{tmp["id"]}','DELETE',expected=204)
        api(8081,f'/beneficiaires/{tmp["id"]}',expected=404)
    check('V1/V6 - beneficiaires et contacts CRUD, email, dossier et idempotence',beneficiaries)
    def protocols():
        body={'type':'TEST_'+run.upper(),'etapes':'Verifier puis informer','niveauPriorite':3}
        p=api(8082,'/api/protocols','POST',body,key('protocol'),expected=201);context['type']=body['type'];context['protocol']=p['id']
        assert api(8082,'/api/protocols','POST',body,key('protocol'),expected=201)==p
        api(8082,f'/api/protocols/{p["id"]}','PUT',dict(body,etapes='Etapes modifiees'))
        assert api(8082,'/api/protocols/type/'+body['type'])['id']==p['id']
        api(8082,'/api/protocols','POST',{'type':'','etapes':'','niveauPriorite':0},expected=400)
        tmp=api(8082,'/api/protocols','POST',dict(body,type='TMP_'+run.upper()),key('tmp-p'),expected=201)
        api(8082,f'/api/protocols/{tmp["id"]}','DELETE',expected=204)
        api(8082,f'/api/protocols/{tmp["id"]}',expected=404)
    check('V1 - protocoles CRUD et validation',protocols)
    def chain():
        # Missing remote reference must not become a fake beneficiary.
        body={'beneficiaryId':context['beneficiary'],'type':context['type'],'gravite':'ELEVEE'}
        api(8082,'/api/alerts','POST',dict(body,beneficiaryId=99999999),key('bad-alert'),expected=404)
        a=api(8082,'/api/alerts','POST',body,key('alert'),expected=201);context['alert']=a['id']
        assert api(8082,'/api/alerts','POST',body,key('alert'),expected=201)==a
        api(8082,'/api/alerts','POST',dict(body,gravite='CRITIQUE'),key('alert'),expected=409)
        def intervention():
            return next((i for i in api(8083,'/interventions') if i['alertId']==a['id']),None)
        i=eventually(intervention,90);context['intervention']=i['id']
        assert i['assistantId']==context['assistant'] or api(8084,f'/api/utilisateurs/{i["assistantId"]}')['role']=='ASSISTANT'
        eventually(lambda:api(8082,f'/api/alerts/{a["id"]}')['statut']=='PRISE_EN_CHARGE',60)
        enriched=api(8082,f'/api/alerts/{a["id"]}/enriched');assert enriched['beneficiary']['id']==context['beneficiary'];assert enriched['emergencyContacts'][0]['id']==context['contact']
        detail=api(8083,f'/interventions/{i["id"]}/detail');assert detail['alerteType']==context['type']
        # Same alert can never produce two interventions.
        api(8083,'/interventions/new','POST',{'alertId':a['id'],'assistantId':context['assistant']},key('duplicate-intervention'),expected=409)
        api(8083,f'/interventions/{i["id"]}','PUT',{'assistantId':99999999},expected=404)
        api(8083,f'/interventions/{i["id"]}/cloture','PUT',expected=409)
        api(8083,f'/interventions/{i["id"]}/arrivee','PUT')
        api(8083,f'/interventions/{i["id"]}/compte-rendu','PUT',{'compteRendu':'Intervention terminee pour le test '+run})
        api(8083,f'/interventions/{i["id"]}/cloture','PUT')
        eventually(lambda:api(8082,f'/api/alerts/{a["id"]}')['statut']=='RESOLUE',60)
        def notifications():
            rows=api(8085,f'/notifications?alertId={a["id"]}')
            return rows if len(rows)==2 else None
        n=eventually(notifications,60)
        assert {v['type'] for v in n}=={'PRISE_EN_CHARGE','RESOLUTION'}
        assert all(v['contactEmail']==run+'@example.test' for v in n)
        def mails():
            messages=request('GET','http://localhost:8025/api/v1/messages')[0]['messages']
            return [m for m in messages if any(t['Address']==run+'@example.test' for t in m.get('To',[]))]
        messages=eventually(lambda: (v if len(v:=mails())>=2 else None),30)
        texts=[request('GET','http://localhost:8025/api/v1/message/'+m['ID'])[0]['Text'] for m in messages]
        assert any('prise en charge' in t for t in texts)
        assert any('Intervention terminee' in t for t in texts)
        # Replayed Kafka events must not alter an already-completed state or send another email.
        context['notifications']=n
    check('V4/V5 - Feign reel, cycle Kafka complet et deux mails recus en SMTP',chain)
    def gateway():
        for m,path,auth in [('service-beneficiaires','/beneficiaires',False),('service-alertes','/api/alerts',False),('service-interventions','/interventions',False),('service-utilisateurs','/api/utilisateurs',True),('service-notification','/notifications',False)]:
            eventually(lambda:api(8080,'/'+m+path,auth=auth) is not None,60)
    check('V3 - routage Gateway des cinq services',gateway)
    def broker_replay():
        rows=context['notifications'];a=context['alert'];i=context['intervention']
        row=next(n for n in rows if n['type']=='PRISE_EN_CHARGE')
        event={'eventId':row['eventId'],'alertId':a,'interventionId':i,'beneficiaryId':context['beneficiary'],'assistantId':context['assistant']}
        kafka_home=os.environ.get('TELECARE_KAFKA_HOME')
        if kafka_home:
            cmd=['sh',str(pathlib.Path(kafka_home)/'bin/kafka-console-producer.sh'),'--bootstrap-server','localhost:9092','--topic','intervention-assigned']
        else:
            cmd=['docker','compose','-p','telecare-validation','exec','-T','kafka','/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server','localhost:9092','--topic','intervention-assigned']
        subprocess.run(cmd,input=(json.dumps(event)+'\n').encode(),cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True,timeout=45)
        time.sleep(3)
        assert api(8082,f'/api/alerts/{a}')['statut']=='RESOLUE'
        assert len(api(8085,f'/notifications?alertId={a}'))==2
    check('V6 - evenement Kafka rejoue sans doublon ni regression de statut',broker_replay)

    def empty_contacts():
        b=api(8081,'/beneficiaires','POST',{'nom':'Sans contact','age':80,'adresse':'Test','telephone':'0600000000'},key('empty-b'),expected=201)
        # Feign client is tested directly via a new alert; no mock contacts should appear.
        a=api(8082,'/api/alerts','POST',{'beneficiaryId':b['id'],'type':context['type'],'gravite':'ELEVEE'},key('empty-a'),expected=201)
        assert api(8082,f'/api/alerts/{a["id"]}/enriched')['emergencyContacts']==[]
    check('V4 - aucun faux contact quand la liste est vide',empty_contacts)
    if with_faults:
        def outage():
            stop_module('service-beneficiaires')
            try:
                for i in range(4):
                    start=time.monotonic()
                    api(8082,f'/api/alerts/{context["alert"]}/enriched',expected=503)
                    assert time.monotonic()-start<12,'Appel non borne'
                metrics=api(8082,'/actuator/circuitbreakers');text=json.dumps(metrics)
                assert 'beneficiaires-service' in text
                assert 'OPEN' in text, 'Le circuit devrait etre ouvert apres les pannes repetees'
            finally:start_module('service-beneficiaires')
            eventually(lambda:api(8082,f'/api/alerts/{context["alert"]}/enriched')['beneficiary']['id']==context['beneficiary'],90)
        check('V6 - panne beneficiaires, 503, appels bornes et reprise apres redemarrage',outage)
        def persistence():
            body={'beneficiaryId':context['beneficiary'],'type':context['type'],'gravite':'ELEVEE'}
            before=api(8082,'/api/alerts','POST',body,key('persist-alert'),expected=201)
            eventually(lambda:api(8082,f'/api/alerts/{before["id"]}')["statut"]=='PRISE_EN_CHARGE',60)
            stop_module('service-alertes');start_module('service-alertes')
            after=api(8082,'/api/alerts','POST',body,key('persist-alert'),expected=201)
            assert before==after,'La cle doit survivre au redemarrage'
        check('V6 - replay alerte persistant apres redemarrage',persistence)
    # Persist evidence even on successful runs; failure also writes a partial report in main().

def run_build():
    cmd=['cmd','/c','mvnw.cmd'] if os.name=='nt' else ['sh','mvnw']
    # Optional settings used only by CI/execution environments; never embedded in the project.
    if os.environ.get('TELECARE_MAVEN_SETTINGS'):cmd+=['-s',os.environ['TELECARE_MAVEN_SETTINGS']]
    cmd+=['-B','-T','2','verify']
    with open(RUN/'build.log','wb') as log:
        print('Compilation et tests Maven (logs: .runtime/build.log)...',flush=True)
        result=subprocess.run(cmd,cwd=ROOT,stdout=log,stderr=subprocess.STDOUT)
    if result.returncode:raise Failure('Echec Maven. Voir .runtime/build.log')

def main():
    global KEEP
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action',choices=['all','build','start','test','stop'],nargs='?',default='all')
    parser.add_argument('--external-infra',action='store_true',help='Kafka 9092 et SMTP/Mailpit 1025/8025 deja disponibles')
    parser.add_argument('--skip-build',action='store_true')
    parser.add_argument('--keep-running',action='store_true')
    parser.add_argument('--no-faults',action='store_true')
    args=parser.parse_args();RUN.mkdir(exist_ok=True);REPORT.mkdir(exist_ok=True);(RUN/'data').mkdir(exist_ok=True)
    KEEP=args.keep_running or args.action=='start'
    if args.action=='stop':
        # Stop only PIDs previously launched by this checkout. Never stop an arbitrary port owner.
        if (RUN/'pids.json').exists():
            for pid in json.loads((RUN/'pids.json').read_text()).values():
                try:
                    if os.name=='nt':subprocess.run(['taskkill','/PID',str(pid),'/T'],capture_output=True)
                    else:os.kill(pid,15)
                except ProcessLookupError:pass
            (RUN/'pids.json').unlink()
        return 0
    try:
        if args.action in ['all','start','build'] and not args.skip_build:run_build()
        if args.action=='build':print('Compilation et tests Maven reussis.');return 0
        if args.action in ['all','start']:
            occupied=[port for port in PORTS.values() if tcp(port)]
            if occupied:raise Failure('Arreter les anciennes instances avant le lancement. Ports occupes: '+str(occupied))
            if not args.external_infra:
                subprocess.run(['docker','compose','-p','telecare-validation','up','-d','--wait'],cwd=ROOT,check=True)
            for port in [9092,1025,8025]:eventually(lambda p=port:tcp(p),90)
            for mod in MODULES:(RUN/(mod+'.log')).write_bytes(b'')
            for mod in MODULES:start_module(mod)
        if args.action in ['all','test']:scenarios(with_faults=not args.no_faults and args.action!='test')
        if args.action=='start':print('Applications en cours. python scripts/telecare.py stop pour arreter.')
        return 0
    except (Exception,KeyboardInterrupt) as e:
        print('ECHEC: '+str(e),flush=True)
        if not RESULTS or RESULTS[-1]['passed']:RESULTS.append({'test':'Preparation ou lancement','passed':False,'detail':str(e)})
        return 1
    finally:
        (REPORT/'test-results.json').write_text(json.dumps(RESULTS,ensure_ascii=False,indent=2))
        export_postman()
        if not KEEP and args.action in ['all','start']:stop_owned()
        print('Rapport: reports/test-results.json',flush=True)
if __name__=='__main__':sys.exit(main())
