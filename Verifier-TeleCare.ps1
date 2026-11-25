param([switch]$KeepRunning,[switch]$SkipBuild,[switch]$ExternalInfra)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { throw 'Python 3.10 ou plus est requis. Installer Python puis relancer ce script.' }
$argsPython = @('scripts/telecare.py', 'all')
if ($KeepRunning) { $argsPython += '--keep-running' }
if ($SkipBuild) { $argsPython += '--skip-build' }
if ($ExternalInfra) { $argsPython += '--external-infra' }
& $python.Source @argsPython
if ($LASTEXITCODE -ne 0) { throw 'Verification echouee. Voir reports/test-results.json et .runtime/*.log.' }
