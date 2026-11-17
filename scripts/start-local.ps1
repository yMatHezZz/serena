param(
    [string]$JavaHome = 'C:\Program Files\Java\jdk-26.0.2',
    [Security.SecureString]$AdminPassword
)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$javaPath = Join-Path $JavaHome 'bin\java.exe'
if (-not (Test-Path -LiteralPath $javaPath)) {
    throw 'JDK não encontrado. Informe -JavaHome com o caminho de um JDK 17 ou superior.'
}
if (-not $AdminPassword) {
    $AdminPassword = Read-Host 'Crie a senha do administrador (mínimo de 12 caracteres)' -AsSecureString
}
$plainPassword = [System.Net.NetworkCredential]::new('', $AdminPassword).Password
if ($plainPassword.Length -lt 12 -or [Text.Encoding]::UTF8.GetByteCount($plainPassword) -gt 72) {
    throw 'A senha precisa ter pelo menos 12 caracteres e até 72 bytes UTF-8.'
}
$previousPassword = $env:ADMIN_PASSWORD
Push-Location -LiteralPath $projectPath
try {
    $env:JAVA_HOME = $JavaHome
    $env:PATH = "$(Join-Path $JavaHome 'bin');$env:PATH"
    & (Join-Path $PSScriptRoot 'test-db.ps1') start
    $env:DB_URL = 'jdbc:postgresql://127.0.0.1:55440/serene_test'
    $env:DB_USERNAME = 'serene_test'
    # O cluster sintético usa trust em loopback e ignora esta senha fictícia.
    $env:DB_PASSWORD = 'somente-teste-local'
    $env:ADMIN_USERNAME = 'admin'
    $env:ADMIN_PASSWORD = $plainPassword
    $env:SERVER_ADDRESS = '127.0.0.1'
    $env:PORT = '8082'
    $plainPassword = $null
    Write-Output 'Ambiente local com dados fictícios. Acesse http://127.0.0.1:8082/ após a mensagem Started.'
    Write-Output 'Login administrativo: admin. Use a senha que acabou de informar. Ctrl+C encerra a aplicação.'
    & (Join-Path $projectPath 'mvnw.cmd') spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'A aplicação não iniciou. Consulte a primeira mensagem Caused by acima.' }
} finally {
    $plainPassword = $null
    $env:ADMIN_PASSWORD = $previousPassword
    Pop-Location
}
