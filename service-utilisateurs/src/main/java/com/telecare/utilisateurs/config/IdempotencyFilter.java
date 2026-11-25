package com.telecare.utilisateurs.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

/** Durable HTTP replay. Business changes and replay record commit in one DB transaction. */
@Component
@Order(100)
public class IdempotencyFilter extends OncePerRequestFilter {
 private final JdbcTemplate jdbc;
 private final TransactionTemplate transactions;
 private final ConcurrentHashMap<String,Object> locks = new ConcurrentHashMap<>();
 public IdempotencyFilter(JdbcTemplate jdbc, PlatformTransactionManager manager) {
  this.jdbc=jdbc; this.transactions=new TransactionTemplate(manager);
  jdbc.execute("CREATE TABLE IF NOT EXISTS http_idempotency (request_key VARCHAR(512) PRIMARY KEY, fingerprint VARCHAR(64) NOT NULL, response_status INT NOT NULL, response_body CLOB NOT NULL, response_type VARCHAR(255), location VARCHAR(1024))");
 }
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String key=req.getHeader("Idempotency-Key");
  if(!"POST".equals(req.getMethod()) || key==null){ chain.doFilter(req,res); return; }
  if(key.isBlank() || key.length()>100){error(res,400,"Idempotency-Key doit contenir entre 1 et 100 caracteres");return;}
  String scope=req.getMethod()+":"+req.getRequestURI()+":"+key;
  byte[] body=req.getInputStream().readAllBytes();
  String hash;
  try{hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));}catch(Exception e){throw new ServletException(e);}
  synchronized(locks.computeIfAbsent(scope,k->new Object())){
   var rows=jdbc.queryForList("SELECT * FROM http_idempotency WHERE request_key=?",scope);
   if(!rows.isEmpty()){
    var row=rows.get(0);
    if(!hash.equals(row.get("FINGERPRINT"))){error(res,409,"Cette cle a deja ete utilisee avec des donnees differentes");return;}
    res.setStatus(((Number)row.get("RESPONSE_STATUS")).intValue());
    res.setContentType((String)row.get("RESPONSE_TYPE"));res.setCharacterEncoding("UTF-8");
    if(row.get("LOCATION")!=null)res.setHeader("Location",(String)row.get("LOCATION"));
    res.setHeader("Idempotency-Replayed","true");
    String saved=jdbc.queryForObject("SELECT response_body FROM http_idempotency WHERE request_key=?",String.class,scope);
    res.getWriter().write(saved);return;
   }
   var cached=new ContentCachingResponseWrapper(res);
   var wrapped=new HttpServletRequestWrapper(req){
    @Override public ServletInputStream getInputStream(){
     var input=new ByteArrayInputStream(body);
     return new ServletInputStream(){
      public int read(){return input.read();}
      public boolean isFinished(){return input.available()==0;}
      public boolean isReady(){return true;}
      public void setReadListener(ReadListener listener){throw new UnsupportedOperationException("Synchronous REST requests only");}
     };
    }
    @Override public BufferedReader getReader(){return new BufferedReader(new InputStreamReader(getInputStream(),StandardCharsets.UTF_8));}
   };
   try{
    transactions.executeWithoutResult(tx->{
     try{chain.doFilter(wrapped,cached);}catch(IOException|ServletException e){throw new IllegalStateException(e);}
     if(cached.getStatus()>=200 && cached.getStatus()<300){
      jdbc.update("INSERT INTO http_idempotency(request_key,fingerprint,response_status,response_body,response_type,location) VALUES(?,?,?,?,?,?)",scope,hash,cached.getStatus(),new String(cached.getContentAsByteArray(),StandardCharsets.UTF_8),cached.getContentType(),cached.getHeader("Location"));
     }
    });
   }catch(org.springframework.transaction.UnexpectedRollbackException ex){
    if(cached.getStatus()<400)throw ex;
   }
   cached.copyBodyToResponse();
  }
 }
 private void error(HttpServletResponse res,int status,String message)throws IOException{
  res.setStatus(status);res.setContentType("application/json");res.setCharacterEncoding("UTF-8");
  res.getWriter().write("{\"status\":"+status+",\"message\":\""+message+"\"}");
 }
}
