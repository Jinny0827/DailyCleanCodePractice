package org.example.cleancode.day21;


/**
 * Day 21: 싱글톤 패턴 개선
 *
 * 문제점:
 * - Thread-safe하지 않은 싱글톤 구현
 * - 전역 상태로 인한 테스트 어려움
 * - 의존성 주입 불가능
 * - 리소스 해제 로직 부재
 */
public class Day21databaseconnection {


    public static void main(String[] args) {

        DatabaseService service = new DatabaseService(DatabaseConnectionManager.getInstance());
    
        // 여러 스레드 동시 접근 시나리오
        for(int i =0; i< 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                service.findUser(threadNum);
            }).start();
        }
        
        // 메인 스레드에서도 사용
        service.getProductCount();
        
        // 리소스 정리
        DatabaseConnectionManager.getInstance().close();
    }
}


interface DatabaseConnectable {
    void connect();
    void executeQuery(String sql);
    void disconnect();
    void cleanUp();
    void close();
}

class DatabaseConnectionManager implements DatabaseConnectable {
    private static volatile DatabaseConnectionManager instance;
    private boolean connected = false;
    private final String connectionString = "jdbc:mysql://localhost:3306/mydb";

    private DatabaseConnectionManager() {
        System.out.println("DatabaseConnectionManager 인스턴스 생성");
    }

    public static DatabaseConnectionManager getInstance() {
        if(instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }

        return instance;
    }

    @Override
    public synchronized void connect() {
        if(!connected) {
            System.out.println("[" + Thread.currentThread().getName() + "] 데이터베이스 연결 중...");
            connected = true;
        }
    }

    @Override
    public void executeQuery(String sql) {
        if(!connected) {
            throw new IllegalStateException("데이터베이스가 연결되어 있지 않습니다.");
        }

        System.out.println("[" + Thread.currentThread().getName() + "] 쿼리 실행: " + sql);
    }

    @Override
    public void disconnect() {
        if (connected) {
            System.out.println("[" + Thread.currentThread().getName() + "] 데이터베이스 연결 해제");
            connected = false;
        }
    }

    @Override
    public void cleanUp() {
        disconnect();
        System.out.println("리소스 정리 완료");
    }

    @Override
    public void close() {
        cleanUp();
    }
}

class DatabaseService {
    private final DatabaseConnectable database;

    public DatabaseService(DatabaseConnectable database) {
        this.database = database;
    }

    public void findUser(int userId) {
        database.connect();
        database.executeQuery("SELECT * FROM users WHERE id = " + userId);
        database.disconnect();
    }

    public void getProductCount() {
        database.connect();
        database.executeQuery("SELECT COUNT(*) FROM products");
        database.disconnect();
    }
}


