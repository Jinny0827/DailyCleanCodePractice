package org.example.cleancode.Y_2025.day23;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 23: 의존성 역전 원칙(DIP) 적용
 *
 * 문제점:
 * - UserService가 구체 클래스(EmailSender, Database)에 직접 의존
 * - 단위 테스트가 어려움 (실제 이메일 발송, DB 접근 필요)
 * - 구현 변경 시 UserService도 수정 필요
 * - 유연성 부족
 */
public class Day23UserService {

    public static void main(String[] args) {
        System.out.println("=== 실제 환경 ===");

        // 1번 유저
        MailSender realMailSender = new RealMailSender();
        UserRepository userRepository = new DatabaseUserRepository();
        UserService realService = new UserService(realMailSender, userRepository);

        realService.registerUser("john@example.com", "password123");
        realService.verifyUser("john@example.com");

        // 2번 유저
        System.out.println("\n=== 테스트 환경 ===");
        MailSender mockMailSender = new MockMailSender();
        UserRepository mockRepository = new InMemoryUserRepository();
        UserService testService = new UserService(mockMailSender, mockRepository);

        testService.registerUser("test@example.com", "test123");
        testService.verifyUser("test@example.com");
    }

}


interface MailSender {
    void send(String to, String subject, String body);
}

interface UserRepository {
    void save(User user);
    User findByEmail(String email);
}

class User {
    private final String email;
    private final String password;
    private boolean verified;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.verified = false;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isVerified() {
        return verified;
    }

    public void verify() {
        this.verified = true;
    }
}

class UserService {
    private final MailSender mailSender;
    private final UserRepository userRepository;

    public UserService(MailSender mailsender, UserRepository userRepository) {
        this.mailSender = mailsender;
        this.userRepository = userRepository;
    }

    public void registerUser(String email, String password) {

        // 가입 이메일에 대한 유효성 검사
        validateEmail(email);

        User user = new User(email, password);
        userRepository.save(user);

        mailSender.send(email, "환영합니다", "회원가입을 축하합니다!");

        System.out.println("✓ 회원가입 완료: " + email);
    }

    public void verifyUser(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            System.out.println("사용자를 찾을 수 없습니다.");
            return;
        }

        user.verify();
        mailSender.send(email, "인증 완료", "계정이 인증되었습니다");
        System.out.println("✓ 인증 완료: " + email);
    }


    private void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("잘못된 이메일");
        }
    }
}

class DatabaseUserRepository implements UserRepository {
    @Override
    public void save(User user) {
       System.out.println("💾 [실제 DB 저장] users: " + user.getEmail());
    }

    @Override
    public User findByEmail(String email) {
        System.out.println("🔍 [실제 DB 조회] users: " + email);
        // 실제로는 DB에서 조회
        return new User(email, "hashed-password");
    }
}

class InMemoryUserRepository implements UserRepository {
    private Map<String, User> storage = new HashMap<>();

    @Override
    public void save(User user) {
        storage.put(user.getEmail(), user);
        System.out.println("💾 [메모리 저장] " + user.getEmail());
    }

    @Override
    public User findByEmail(String email) {
        System.out.println("🔍 [메모리 조회] " + email);
        return storage.get(email);
    }
}


class MockMailSender implements MailSender {
    @Override
    public void send(String to, String subject, String body) {
        System.out.println("📧 [테스트용 이메일] " + to + " - " + subject);
    }
}

class RealMailSender implements MailSender {
    @Override
    public void send(String to, String subject, String body) {
        System.out.println("📧 [실제 이메일 발송]");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}





























