# JavaJolt Setup Script
# This script loads environment variables from .env file and starts the application

Write-Host "🚀 JavaJolt Setup Script" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green

# Check if .env file exists
if (!(Test-Path ".env")) {
    Write-Host "❌ Error: .env file not found!" -ForegroundColor Red
    Write-Host "Please create a .env file with your configuration." -ForegroundColor Yellow
    exit 1
}

# Load environment variables from .env file
Write-Host "📝 Loading environment variables from .env..." -ForegroundColor Blue
Get-Content ".env" | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
        Write-Host "  ✓ Set $key" -ForegroundColor Green
    }
}

# Check MySQL connection
Write-Host "🔍 Checking MySQL connection..." -ForegroundColor Blue
$mysqlHost = "localhost"
$mysqlPort = 3306
$mysqlUser = $env:DB_USER
$mysqlPass = $env:DB_PASSWORD
$mysqlDB = $env:DB_NAME

try {
    # Test MySQL connection using mysql command
    $testConnection = & mysql -h $mysqlHost -P $mysqlPort -u $mysqlUser -p$mysqlPass -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ MySQL connection successful" -ForegroundColor Green
    } else {
        Write-Host "  ❌ MySQL connection failed. Please check your credentials." -ForegroundColor Red
        Write-Host "  Make sure MySQL is running and credentials are correct." -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "  ⚠️  MySQL command not found. Skipping connection test." -ForegroundColor Yellow
    Write-Host "  Please ensure MySQL is running and accessible." -ForegroundColor Yellow
}

# Check if required environment variables are set
$requiredVars = @("DB_NAME", "DB_USER", "DB_PASSWORD", "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GMAIL_USERNAME", "GMAIL_APP_PASSWORD")
$missingVars = @()

foreach ($var in $requiredVars) {
    if ([string]::IsNullOrEmpty([Environment]::GetEnvironmentVariable($var, "Process"))) {
        $missingVars += $var
    }
}

if ($missingVars.Count -gt 0) {
    Write-Host "❌ Error: Missing required environment variables:" -ForegroundColor Red
    foreach ($var in $missingVars) {
        Write-Host "  - $var" -ForegroundColor Red
    }
    Write-Host "Please update your .env file with the correct values." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ All environment variables are set!" -ForegroundColor Green

# Start the application
Write-Host "🚀 Starting JavaJolt application..." -ForegroundColor Green
Write-Host "Application will be available at: http://localhost:8080/quiz-app" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""

# Run the application
& .\mvnw.cmd spring-boot:run