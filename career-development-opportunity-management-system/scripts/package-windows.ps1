<#
.SYNOPSIS
    Builds the application and wraps it with jpackage for Windows.

.DESCRIPTION
    Default output is an "app-image": a folder with CareerForge.exe that runs without installing anything,
    and needs no extra tools. Use -Type exe or -Type msi for a real installer; those need the WiX Toolkit 3.x
    on PATH (jpackage in JDK 21 cannot use WiX 4 or newer).

.EXAMPLE
    .\scripts\package-windows.ps1
    .\scripts\package-windows.ps1 -Type exe
#>
param(
    [ValidateSet('app-image', 'exe', 'msi')]
    [string]$Type = 'app-image',
    [string]$Version = '1.0.0'
)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw 'jpackage was not found. Install a full JDK 21 and make sure its bin folder is on PATH.'
}

mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'The Maven build failed.' }

$jar = "career-development-opportunity-management-system-$Version.jar"
if (-not (Test-Path "target/$jar")) { throw "target/$jar was not produced. Check -Version against pom.xml." }

$input = 'target/jpackage-input'
Remove-Item $input -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $input | Out-Null
Copy-Item "target/$jar" $input

# The jar is a classpath application, so jpackage cannot discover every JDK module it needs.
$modules = 'java.base,java.desktop,java.logging,java.naming,java.management,java.prefs,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.unsupported'

$arguments = @(
    '--name', 'CareerForge',
    '--app-version', $Version,
    '--vendor', 'CareerForge',
    '--description', 'Track skills, opportunities, applications and interviews',
    '--input', $input,
    '--main-jar', $jar,
    '--main-class', 'com.studenttracker.fx.FxLauncher',
    '--icon', 'scripts/logo.ico',
    '--add-modules', $modules,
    '--dest', 'dist',
    '--type', $Type
)
if ($Type -ne 'app-image') {
    $arguments += @('--win-menu', '--win-shortcut', '--win-dir-chooser')
}

jpackage @arguments
if ($LASTEXITCODE -ne 0) { throw 'jpackage failed.' }

Write-Host ''
Write-Host "Done. Output is in the dist folder."
Write-Host 'Installed copies read their database settings from %USERPROFILE%\.careerforge\application.properties'
Write-Host '(same keys as config/application.properties.example) or from the DB_* environment variables.'
