package org.example.cleancode.day45;


import org.w3c.dom.events.Event;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Day 45: 이벤트 소싱 패턴
 *
 * 문제점:
 * - 현재 상태만 저장 (변경 이력 손실)
 * - 과거 시점 조회 불가
 * - 이벤트 재생(replay) 미지원
 * - 상태 복원 로직 없음
 * - 동시성 제어 부재
 */
public class Day45EventSourcing {

    public static void main(String[] args) throws InterruptedException {
        BankAccountService service = new BankAccountService();
        
        // 계좌 거래
        System.out.println("=== 계좌 거래 시작 ===");
        service.openAccount("ACC-001", "홍길동");

        service.deposit("ACC-001", 100000);
        Thread.sleep(100);

        // 중간 시점 저장
        long midTime = System.currentTimeMillis();
        Thread.sleep(100);
        
        service.withdraw("ACC-001", 30000);
        Thread.sleep(100);

        service.deposit("ACC-001", 50000);

        // 현재 잔액 조회
        System.out.println("\n=== 현재 잔액 ===");
        System.out.println("현재 잔액: " + service.getBalance("ACC-001") + "원");

        // 과거 시점 잔액 조회
        int pastBalance = service.getBalanceAt("ACC-001", midTime);
        System.out.println("중간 시점 잔액: " + pastBalance + "원");

        // 계좌 전체 복원
        BankAccount account = service.loadAccount("ACC-001");
        System.out.println("소유자: " + account.getOwner());
        System.out.println("현재 잔액: " + account.getBalance() + "원");
        System.out.println("이벤트 버전: " + account.getVersion());


        service.printEventHistory("ACC-001");
    }

}

// 공용 이벤트에 대한 도메인별 인터페이스 처리
interface DomainEvent {
    String getEventId();        // 이벤트 고유 ID
    String getAggregateId();    // 계좌 ID
    long getTimestamp();        // 발생 시간
    int getVersion();           // 이벤트 순서
}


// 계좌 개설
class AccountOpenedEvent implements DomainEvent {
    private final String eventId;
    private final String accountId;
    private final String owner;
    private final long timestamp;
    private final int version;

    public AccountOpenedEvent(String accountId, String owner, int version) {
        this.eventId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.owner = owner;
        this.timestamp = System.currentTimeMillis();
        this.version = version;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getVersion() {
        return version;
    }
}


// 입금
class MoneyDepositedEvent implements DomainEvent {
    private final String eventId;
    private final String accountId;
    private final int amount;
    private final long timestamp;
    private final int version;


    public MoneyDepositedEvent(String accountId, int amount, int version) {
        this.eventId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.amount = amount;
        this.version = version;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAccountId() {
        return accountId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getVersion() {
        return version;
    }
}


// 출금
class MoneyWithdrawnEvent implements DomainEvent {
    private final String eventId;
    private final String accountId;
    private final int amount;
    private final long timestamp;
    private final int version;

    public MoneyWithdrawnEvent(String accountId, int amount, int version) {
        this.eventId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.amount = amount;
        this.version = version;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAccountId() {
        return accountId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getVersion() {
        return version;
    }
}

// 이벤트 저장소
interface EventStore {
    // 이벤트 저장
    void save(DomainEvent event);
    // 특정 계좌에 대한 이벤트 조회 (시간 순)
    List<DomainEvent> getEvents(String aggregateId);
    // 특정 시점 까지의 이벤트 조회 (특정 계좌)
    List<DomainEvent> getEventsUtil(String aggregateId, long timestamp);
}

// 이벤트 저장소 구현체
class InMemoryEventStore implements EventStore {

    // 이벤트 저장소(계좌별로 리스트 관리 / 계좌명, 이벤트 리스트)
    private Map<String, List<DomainEvent>> eventStreams = new HashMap<>();

    @Override
    public void save(DomainEvent event) {
        String aggregateId = event.getAggregateId();

        eventStreams.computeIfAbsent(aggregateId, k -> new ArrayList<>())
                .add(event);

        System.out.println("📝 이벤트 저장: " + event.getClass().getSimpleName());
    }

    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        List<DomainEvent> events = eventStreams.get(aggregateId);
        return events != null ? new ArrayList<>(events) : new ArrayList<>();
    }

    @Override
    public List<DomainEvent> getEventsUtil(String aggregateId, long timestamp) {
        return getEvents(aggregateId).stream()
                .filter(e -> e.getTimestamp() <= timestamp)
                .collect(Collectors.toList());
    }
}


// 이벤트 소싱 방식으로 변경 (리팩터링)
// 기존에는 상태를 직접 변경 -> 이벤트를 발행
class BankAccountService {
    private final EventStore eventStore;

    public BankAccountService() {
        this.eventStore = new InMemoryEventStore();
    }

    // 계좌 개설 (이벤트 발행)
    public void openAccount(String accountId, String owner) {
        AccountOpenedEvent event = new AccountOpenedEvent(accountId, owner, 1);

        eventStore.save(event);

        System.out.println("✓ 계좌 개설: " + accountId);
    }

    // 입금 (이벤트 발행)
    public void deposit(String accountId, int amount) {
        // 현재 계좌 상태 복원
        BankAccount account = loadAccount(accountId);
        
        // 새버전으로 이벤트 생성
        int newVersion = account.getVersion() + 1;
        MoneyDepositedEvent event = new MoneyDepositedEvent(accountId, amount, newVersion);

        // 이벤트 저장
        eventStore.save(event);

        System.out.println("✓ 입금: " + amount + "원");
    }
    
    // 출금 (이벤트 발행 + 비지니스 검증)
    public void withdraw(String accountId, int amount) {
        // 현재 계좌 상태 복원
        BankAccount account = loadAccount(accountId);
        
        // 비지니스 규칙 검증
        if(account.getBalance() < amount) {
            throw new RuntimeException("잔액 부족");
        }

        // 이벤트 발행
        int newVersion = account.getVersion() + 1;
        MoneyWithdrawnEvent event = new MoneyWithdrawnEvent(accountId, amount, newVersion);
        eventStore.save(event);

        System.out.println("✓ 출금: " + amount + "원");
    }

    // 잔액 조회
    public int getBalance(String accountId) {
        BankAccount account = loadAccount(accountId);
        return account.getBalance();
    }
    
    // 계좌 로드 (이벤트 재생)
    public BankAccount loadAccount(String accountId) {
        List<DomainEvent> events = eventStore.getEvents(accountId);
        return BankAccount.fromEvents(events);
    }

    // 특정 시점의 계좌 잔액 조회
    public int getBalanceAt(String accountId, long timestamp) {
        List<DomainEvent> events = eventStore.getEventsUtil(accountId, timestamp);
        BankAccount account = BankAccount.fromEvents(events);

        return account.getBalance();
    }
    
    
    // 전체 이벤트 이력 확인
    public void printEventHistory(String accountId) {
        List<DomainEvent> events = eventStore.getEvents(accountId);

        System.out.println("\n=== 이벤트 히스토리: " + accountId + " ===");
        for(DomainEvent event : events) {
            String eventInfo = formatEvent(event);
            System.out.println(eventInfo);
        }
    }

    private String formatEvent(DomainEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("[v").append(event.getVersion()).append("] ");

        if (event instanceof AccountOpenedEvent) {
            AccountOpenedEvent e = (AccountOpenedEvent) event;
            sb.append("계좌 개설 - 소유자: ").append(e.getOwner());
        }
        else if (event instanceof MoneyDepositedEvent) {
            MoneyDepositedEvent e = (MoneyDepositedEvent) event;
            sb.append("입금 - ").append(e.getAmount()).append("원");
        }
        else if (event instanceof MoneyWithdrawnEvent) {
            MoneyWithdrawnEvent e = (MoneyWithdrawnEvent) event;
            sb.append("출금 - ").append(e.getAmount()).append("원");
        }

        return sb.toString();
    }

    
}


// BankAccount가 이벤트로부터 상태 복원(리팩터링)
class BankAccount {
    private String accountId;
    private String owner;
    private int balance;
    private int version;

    public BankAccount() {
        this.balance = 0;
        this.version = 0;
    }
    
    
    // 이벤트를 받아서 상태 변경
    public void apply(DomainEvent event) {
        // 계좌 개설 이벤트
        if(event instanceof AccountOpenedEvent) {
            AccountOpenedEvent e = (AccountOpenedEvent) event;
            this.accountId = e.getAccountId();
            this.owner = e.getOwner();
            this.balance = 0;
        } else if (event instanceof MoneyDepositedEvent) {
            // 입금 이벤트
            MoneyDepositedEvent e = (MoneyDepositedEvent) event;
            this.balance += e.getAmount();
        } else if (event instanceof  MoneyWithdrawnEvent) {
            // 출금 이벤트
            MoneyWithdrawnEvent e = (MoneyWithdrawnEvent) event;
            this.balance -= e.getAmount();
        }

        // 버전 업데이트
        this.version = event.getVersion();
    }
    
    // 이벤트 리스트로부터 계좌 구성 (정적 이벤트)
    public static BankAccount fromEvents(List<DomainEvent> events) {
        BankAccount account = new BankAccount();

        for (DomainEvent event : events) {
            account.apply(event);
        }

        return account;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    public int getBalance() {
        return balance;
    }

    public int getVersion() {
        return version;
    }
}