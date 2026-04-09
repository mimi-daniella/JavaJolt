# JavaJolt Quiz Application

A modern, full-stack quiz application built with Spring Boot, featuring user authentication, role-based dashboards, and Google OAuth2 integration.

## 🚀 Quick Start

### Prerequisites
- Java 21
- MySQL 8.0+
- Maven

### Setup
1. **Clone and navigate to the project:**
   ```bash
   cd JavaJolt
   ```

2. **Configure environment variables:**
   - Copy `.env` template and fill in your credentials
   - Or follow the detailed setup guide in `SETUP_GUIDE.md`

3. **Test your configuration:**
   ```powershell
   .\test-config.ps1
   ```

4. **Run the application:**
   ```powershell
   .\setup.ps1
   ```

5. **Access the application:**
   - Main app: http://localhost:8080/quiz-app
   - Registration/Login: http://localhost:8080/quiz-app/auth/register

## 📋 Features

### Authentication & Security
- ✅ User registration with email OTP verification
- ✅ Secure login with password encryption
- ✅ Google OAuth2 integration
- ✅ Role-based access control (Admin/User)
- ✅ Password reset functionality
- ✅ Session management

### Dashboards
- ✅ **User Dashboard**: Quiz statistics, quick actions, activity tracking
- ✅ **Admin Dashboard**: User management, question bank, system overview
- ✅ Responsive design with JavaJolt aesthetics

### Technical Stack
- **Backend**: Spring Boot 4.0, Spring Security, Spring Data JPA
- **Database**: MySQL with Hibernate
- **Frontend**: Thymeleaf, Tailwind CSS, JavaScript
- **Email**: Spring Mail with Gmail SMTP
- **OAuth2**: Google OAuth2 Client

## 🛠️ Configuration

### Required Environment Variables
```env
# Database
DB_NAME=java_jolt_db
DB_USER=root
DB_PASSWORD=your_password

# Google OAuth2
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret

# Gmail
GMAIL_USERNAME=your_email@gmail.com
GMAIL_APP_PASSWORD=your_app_password
```

### Database Setup
1. Install MySQL
2. Create database: `java_jolt_db`
3. Update credentials in `.env`

### Google OAuth2 Setup
1. Create project in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google+ API
3. Create OAuth2 credentials
4. Add redirect URI: `http://localhost:8080/quiz-app/login/oauth2/code/google`

### Gmail Setup
1. Enable 2-Factor Authentication
2. Generate App Password
3. Use app password (not regular password) in configuration

## 📁 Project Structure

```
JavaJolt/
├── src/main/java/com/daniella/
│   ├── config/          # Spring configuration
│   ├── controller/      # Web controllers
│   ├── dto/            # Data transfer objects
│   ├── entity/         # JPA entities
│   ├── enums/          # Application enums
│   ├── repository/     # Data repositories
│   ├── security/       # Security configuration
│   └── service/        # Business logic
├── src/main/resources/
│   ├── templates/      # Thymeleaf templates
│   └── application.properties
├── .env                # Environment variables (gitignored)
├── setup.ps1          # PowerShell setup script
├── setup.bat          # Batch setup script
├── test-config.ps1    # Configuration test script
└── SETUP_GUIDE.md     # Detailed setup instructions
```

## 🔧 Development

### Running Tests
```bash
./mvnw test
```

### Building for Production
```bash
./mvnw clean package
```

### IDE Setup
- Import as Maven project
- Ensure Java 21 is configured
- Install Lombok plugin (if using an IDE)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

- Check `SETUP_GUIDE.md` for detailed setup instructions
- Review application logs for errors
- Ensure all environment variables are properly configured
- Verify Google Cloud Console and Gmail settings

## 🎯 Roadmap

- [ ] Quiz creation and management
- [ ] Real-time quiz sessions
- [ ] Leaderboards and achievements
- [ ] Admin analytics dashboard
- [ ] Mobile-responsive design improvements
- [ ] API documentation

---

**Happy quizzing with JavaJolt! 🚀**