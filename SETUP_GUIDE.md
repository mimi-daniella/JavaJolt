# JavaJolt Setup Guide
## Complete Setup Instructions

### Prerequisites
- Java 21 installed
- MySQL 8.0+ installed and running
- Maven installed
- A Google account
- A Gmail account

---

## Step 1: Database Setup

### Install and Start MySQL
1. Download MySQL from https://dev.mysql.com/downloads/mysql/
2. Install MySQL Server
3. Start MySQL service
4. Create a database named `java_jolt_db`

### Alternative: Use XAMPP
1. Download XAMPP from https://www.apachefriends.org/
2. Start Apache and MySQL modules
3. Open phpMyAdmin (http://localhost/phpmyadmin)
4. Create database: `java_jolt_db`

---

## Step 2: Google OAuth2 Setup

### Create Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google+ API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google+ API" and enable it

### Create OAuth2 Credentials
1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Configure OAuth consent screen:
   - User Type: External
   - App name: JavaJolt Quiz App
   - User support email: your-email@gmail.com
   - Developer contact: your-email@gmail.com
   - Save and Continue
4. Add scopes (optional for basic setup)
5. Add test users (your Gmail address)
6. Create OAuth2 Client ID:
   - Application type: Web application
   - Name: JavaJolt Web Client
   - Authorized redirect URIs: `http://localhost:8080/quiz-app/login/oauth2/code/google`
   - Click Create

### Get Your Credentials
- Copy the Client ID and Client Secret
- These will be your `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`

---

## Step 3: Gmail App Password Setup

### Enable 2-Factor Authentication
1. Go to [Google Account Settings](https://myaccount.google.com/)
2. Security > 2-Step Verification
3. Enable 2-Step Verification if not already enabled

### Generate App Password
1. Go to Security > 2-Step Verification > App passwords
2. Sign in with your Google account
3. Select "Mail" and "Other (custom name)"
4. Enter "JavaJolt Quiz App" as custom name
5. Click Generate
6. Copy the 16-character password (ignore spaces)

### Important Notes
- App passwords are only shown once - save it immediately
- This password will be your `GMAIL_APP_PASSWORD`
- Your regular Gmail password won't work for SMTP

---

## Step 4: Configure Environment Variables

### Update .env File
Edit the `.env` file in your project root with your actual values:

```env
# Database Configuration
DB_NAME=java_jolt_db
DB_USER=root
DB_PASSWORD=your_mysql_root_password

# Google OAuth2 Configuration
GOOGLE_CLIENT_ID=your_actual_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_actual_google_client_secret

# Gmail Configuration
GMAIL_USERNAME=your_gmail_address@gmail.com
GMAIL_APP_PASSWORD=your_16_character_app_password
```

### Alternative: Set System Environment Variables
You can also set these as system environment variables:

**Windows PowerShell (Admin):**
```powershell
[Environment]::SetEnvironmentVariable("DB_NAME", "java_jolt_db", "Machine")
[Environment]::SetEnvironmentVariable("DB_USER", "root", "Machine")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password", "Machine")
[Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_ID", "your_client_id", "Machine")
[Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_SECRET", "your_client_secret", "Machine")
[Environment]::SetEnvironmentVariable("GMAIL_USERNAME", "your_email@gmail.com", "Machine")
[Environment]::SetEnvironmentVariable("GMAIL_APP_PASSWORD", "your_app_password", "Machine")
```

---

## Step 5: Run the Application

### Using Setup Script (Recommended)
```powershell
.\setup.ps1
```

### Manual Start
```powershell
# Load environment variables
Get-Content ".env" | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

# Start application
.\mvnw.cmd spring-boot:run
```

---

## Step 6: Test the Application

1. Open browser: http://localhost:8080/quiz-app
2. Test registration with OTP email
3. Test Google OAuth login
4. Test admin/user dashboards

---

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check username/password in .env
- Verify database `java_jolt_db` exists

### OAuth2 Issues
- Verify redirect URI matches: `http://localhost:8080/quiz-app/login/oauth2/code/google`
- Check Client ID and Secret are correct
- Ensure Google+ API is enabled

### Email Issues
- Verify Gmail credentials
- Check app password is correct (16 characters, no spaces)
- Ensure 2FA is enabled on Gmail account

### Port Issues
- If port 8080 is busy, change in application.properties:
  ```
  server.port=8081
  ```

---

## Security Notes
- Never commit .env file to version control
- Use strong passwords for database
- Keep OAuth2 credentials secure
- Regularly rotate app passwords

---

## Support
If you encounter issues:
1. Check application logs in console
2. Verify all environment variables are set
3. Ensure all prerequisites are installed
4. Check Google Cloud Console configuration

##Daniella
- Added env variables
- Changed port from smtp(587) to ssl(465)
- Added mimemessage helper for customizing email ui