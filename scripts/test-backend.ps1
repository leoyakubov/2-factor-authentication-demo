$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"
$origin = Get-Location

try {
  Set-Location $backend
  if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
  }

  & ".\mvnw.cmd" test
}
finally {
  Set-Location $origin
}
