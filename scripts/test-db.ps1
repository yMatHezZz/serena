param(
    [ValidateSet('start','stop')] [string]$Action = 'start',
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\16\bin'
)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$localPath = Join-Path $projectPath '.local'
$dataPath = Join-Path $localPath 'pgdata'
if ($Action -eq 'stop') {
    & (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataPath stop -m fast
    exit $LASTEXITCODE
}
New-Item -ItemType Directory -Path $localPath -Force | Out-Null
if (-not (Test-Path -LiteralPath (Join-Path $dataPath 'PG_VERSION'))) {
    # Exclusivo para dados fictícios de testes em loopback; não usar este cluster com dados reais.
    & (Join-Path $PostgresBin 'initdb.exe') -D $dataPath -U serene_test -A trust --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao inicializar PostgreSQL de testes.' }
}
& (Join-Path $PostgresBin 'pg_ctl.exe') -D $dataPath status *> $null
if ($LASTEXITCODE -ne 0) {
    $logPath = Join-Path $localPath 'postgres.log'
    $arguments = '-D "{0}" -l "{1}" -o "-h 127.0.0.1 -p 55440" start' -f $dataPath, $logPath
    $startup = Start-Process -FilePath (Join-Path $PostgresBin 'pg_ctl.exe') -ArgumentList $arguments -WindowStyle Hidden -PassThru
    $startup.WaitForExit()
    if ($startup.ExitCode -ne 0) { throw "Falha ao iniciar PostgreSQL de testes. Consulte $logPath." }
}
$exists = & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p 55440 -U serene_test -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='serene_test'"
if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar o cluster de testes.' }
if ($exists -ne '1') {
    & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p 55440 -U serene_test serene_test
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao criar serene_test.' }
}
Write-Output 'Banco sintético de testes disponível em 127.0.0.1:55440/serene_test.'
