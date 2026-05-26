$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root "frontend"
$origin = Get-Location

try {
  Set-Location $frontend
  if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
  }
  if (-not (Test-Path "node_modules")) {
    npm install
  }

  $env:CI = "true"
  npm run build:ci
}
finally {
  Set-Location $origin
}
