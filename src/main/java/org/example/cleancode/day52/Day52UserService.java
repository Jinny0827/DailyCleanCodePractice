package org.example.cleancode.day52;

/**
 * Day 52: 의존성 주입과 테스트 가능한 Repository
 *
 */
public class Day52UserService {
    private UserRepository userRepository;
    private EmailService emailService;
    private LogService logService;

    public Day52UserService(UserRepository userRepository, EmailService emailService, LogService logService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.logService = logService;
    }

    public static void main(String[] args) {
        Day52UserService service = new Day52UserService(
                new DatabaseConnection("localhost"),  // 실제 DB
                new EmailSender("smtp.gmail.com"),   // 실제 이메일
                new Logger("/var/log/app.log")       // 실제 로그
        );

        Day52UserService service2 = new Day52UserService(
                new MockRepository(),  // 가짜 DB
                new MockEmail(),       // 가짜 이메일
                new MockLogger()       // 가짜 로그
        );

        service.register("user@example.com", "password123");
        service2.register("user@example.com", "password123");
    }

    public void register(String email, String password) {
        logService.log("Registering user: " + email);

        if(userRepository.exists(email)) {
            logService.log("User already exists");
            throw new RuntimeException("User exists");
        }

        String hashedPw = hashPassword(password);

        userRepository.save(email, hashedPw);

        String welcomeMsg = "Welcome to our service!";
        emailService.sendEmail(email, "Welcome", welcomeMsg);  // sendEmail로 변경

        logService.log("User registered successfully");
    }

    private String hashPassword(String password) {
        return "hashed_" + password;
    }
}

// Mock 클래스
class MockRepository implements UserRepository {
    @Override
    public boolean exists(String email) {
        return false;
    }

    @Override
    public void save(String email, String hashedPassword) {

    }
}

class MockEmail implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {

    }
}

class MockLogger implements LogService {
    @Override
    public void log(String message) {

    }
}




// DB 관련 작업 추상화
interface UserRepository {
    boolean exists(String email);
    void save(String email, String hashedPassword);
}

// 이메일 발송 추상화
interface EmailService {
    void sendEmail(String to, String subject, String body);
}

// 로깅 추상화
interface LogService {
    void log(String message);
}


class DatabaseConnection implements UserRepository {
    private String connectionString;

    public DatabaseConnection(String connectionString) {
        this.connectionString = connectionString;
        System.out.println("DB Connected to: " + connectionString);
    }

    @Override
    public boolean exists(String email) {
        System.out.println("Checking if user exists: " + email);
        return false;
    }

    @Override
    public void save(String email, String hashedPassword) {
        // 실제로는 DB INSERT
        System.out.println("Saving user: " + email);
    }
}


class EmailSender implements EmailService  {
    private String smtpServer;

    public EmailSender(String smtpServer) {
        this.smtpServer = smtpServer;
        System.out.println("Email client connected to: " + smtpServer);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
    }
}


class Logger implements LogService {
    private String logFilePath;

    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(String message) {
        // 실제로는 파일에 기록
        System.out.println("[LOG] " + message);
    }
}
