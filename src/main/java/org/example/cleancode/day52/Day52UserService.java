package org.example.cleancode.day52;

/**
 * Day 52: 의존성 주입과 테스트 가능한 Repository
 *
 */
public class Day52UserService {
    private DatabaseConnection db = new DatabaseConnection("localhost:3306");
    private EmailSender emailer = new EmailSender("smtp.gmail.com");
    private Logger logger = new Logger("/var/log/app.log");

    public void registerUser(String email, String password) {
        logger.log("Registering user: " + email);

        if (db.userExists(email)) {
            logger.log("User already exists");
            throw new RuntimeException("User exists");
        }

        String hashedPw = hashPassword(password);
        db.saveUser(email, hashedPw);

        String welcomeMsg = "Welcome to our service!";
        emailer.send(email, "Welcome", welcomeMsg);

        logger.log("User registered successfully");
    }

    private String hashPassword(String password) {
        return "hashed_" + password;
    }
}
// DatabaseConnection.java
class DatabaseConnection {
    private String connectionString;

    public DatabaseConnection(String connectionString) {
        this.connectionString = connectionString;
        System.out.println("DB Connected to: " + connectionString);
    }

    public boolean userExists(String email) {
        // 실제로는 DB 쿼리 실행
        System.out.println("Checking if user exists: " + email);
        return false;
    }

    public void saveUser(String email, String hashedPassword) {
        // 실제로는 DB INSERT
        System.out.println("Saving user: " + email);
    }
}

// EmailSender.java
class EmailSender {
    private String smtpServer;

    public EmailSender(String smtpServer) {
        this.smtpServer = smtpServer;
        System.out.println("Email client connected to: " + smtpServer);
    }

    public void send(String to, String subject, String body) {
        // 실제로는 이메일 발송
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
    }
}

// Logger.java
class Logger {
    private String logFilePath;

    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(String message) {
        // 실제로는 파일에 기록
        System.out.println("[LOG] " + message);
    }
}


