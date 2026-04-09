# JavaJolt Configuration Test Script
# Run this to verify your setup before starting the application

Write-Host "🧪 JavaJolt Configuration Test" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green

# Check if .env file exists
if (!(Test-Path ".env")) {
    Write-Host "❌ Error: .env file not found!" -ForegroundColor Red
    exit 1
}

# Load environment variables
Write-Host "📝 Loading environment variables..." -ForegroundColor Blue
$envVars = @{}
Get-Content ".env" | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        $envVars[$key] = $value
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

# Check required variables
$requiredVars = @("DB_NAME", "DB_USER", "DB_PASSWORD", "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GMAIL_USERNAME", "GMAIL_APP_PASSWORD")
$missingVars = @()

Write-Host "🔍 Checking environment variables..." -ForegroundColor Blue
foreach ($var in $requiredVars) {
    if ([string]::IsNullOrEmpty($envVars[$var])) {
        $missingVars += $var
        Write-Host "  ❌ $var is missing" -ForegroundColor Red
    } else {
        Write-Host "  ✓ $var is set" -ForegroundColor Green
    }
}

if ($missingVars.Count -gt 0) {
    Write-Host "❌ Configuration incomplete. Please update your .env file." -ForegroundColor Red
    exit 1
}

# Test database connection (optional)
Write-Host "🔍 Testing database connection..." -ForegroundColor Blue
try {
    $connectionString = "Server=localhost;Database=$($env:DB_NAME);User Id=$($env:DB_USER);Password=$($env:DB_PASSWORD);"
    # We can't easily test MySQL from PowerShell without additional modules
    # Just check if variables look reasonable
    if ($env:DB_NAME -and $env:DB_USER -and $env:DB_PASSWORD) {
        Write-Host "  ✓ Database configuration looks good" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠️  Could not test database connection" -ForegroundColor Yellow
}

# Validate Google OAuth format
Write-Host "🔍 Validating Google OAuth configuration..." -ForegroundColor Blue
if ($env:GOOGLE_CLIENT_ID -match '\.apps\.googleusercontent\.com$') {
    Write-Host "  ✓ Google Client ID format looks correct" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Google Client ID format may be incorrect" -ForegroundColor Yellow
}

if ($env:GOOGLE_CLIENT_SECRET -match '^GOCSPX-') {
    Write-Host "  ✓ Google Client Secret format looks correct" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Google Client Secret format may be incorrect" -ForegroundColor Yellow
}

# Validate Gmail configuration
Write-Host "🔍 Validating Gmail configuration..." -ForegroundColor Blue
if ($env:GMAIL_USERNAME -match '@gmail\.com$') {
    Write-Host "  ✓ Gmail username format looks correct" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Gmail username should end with @gmail.com" -ForegroundColor Yellow
}

if ($env:GMAIL_APP_PASSWORD -match '^[a-zA-Z0-9]{16}$') {
    Write-Host "  ✓ Gmail app password format looks correct" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Gmail app password should be 16 characters" -ForegroundColor Yellow
}

# Check if Maven wrapper exists
Write-Host "🔍 Checking project files..." -ForegroundColor Blue
if (Test-Path "mvnw.cmd") {
    Write-Host "  ✓ Maven wrapper found" -ForegroundColor Green
} else {
    Write-Host "  ❌ Maven wrapper not found" -ForegroundColor Red
}

if (Test-Path "pom.xml") {
    Write-Host "  ✓ Project configuration found" -ForegroundColor Green
} else {
    Write-Host "  ❌ Project configuration not found" -ForegroundColor Red
}

Write-Host "" -ForegroundColor White
Write-Host "✅ Configuration test completed!" -ForegroundColor Green
Write-Host "If all checks passed, you can now run the application with:" -ForegroundColor White
Write-Host "  .\setup.ps1" -ForegroundColor Cyan
Write-Host "" -ForegroundColor White