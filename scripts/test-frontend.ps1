$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root "frontend"
$origin = Get-Location

function Import-EnvFile {
  param([string]$Path)

  Get-Content -LiteralPath $Path | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith("#") -or -not ($line -match "^(?<name>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$")) {
      return
    }

    $value = $Matches.value.Trim()
    if ($value.StartsWith('"') -and $value.EndsWith('"')) {
      $value = $value.Substring(1, $value.Length - 2)
    }

    Set-Item -Path "Env:$($Matches.name)" -Value $value
  }
}

try {
  Set-Location $frontend
  if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
  }
  Import-EnvFile ".env"
  if (-not (Test-Path "node_modules/jest")) {
    & npm ci --no-audit --no-fund
    if ($LASTEXITCODE -ne 0) {
      throw "npm ci failed while preparing the frontend dependencies."
    }
  }
  if (-not (Test-Path "node_modules/jest")) {
    throw "jest is still missing after npm ci. Run 'cd frontend; npm install' and try again."
  }

  $env:CI = "true"
  & npm run test:ci
}
finally {
  Set-Location $origin
}
