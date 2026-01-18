package org.example.cleancode.Y_2025.day37;


import java.util.*;

/**
 * Day 37: 메모리 누수 방지
 *
 * 문제점:
 * - 이벤트 리스너가 해제되지 않음
 * - 타이머가 정리되지 않음
 * - 콜백 참조가 계속 유지됨
 * - 리소스 정리 시점이 불명확
 *
 * 폴링 = 주기적으로 데이터를 확인하는 것
 * 주기적(타이머)를 사용하고 정리하고 반복
 *
 */
public class Day37RealTimeMonitor {

    public static void main(String[] args) throws InterruptedException {
        DataMonitor monitor = new DataMonitor();


        // 구독자 등록
        Subscriber sub1 = new Subscriber("SUB-001");
        Subscription subscription1 = monitor.subscribe(sub1);

        PollingTask polling = new PollingTask(monitor);
        polling.start();

        // 3초 대기 (데이터 3번 업데이트됨)
        Thread.sleep(3000);

        // 폴링 중지
        polling.dispose();

        // 1초 더 대기(업데이트 안됨)
        Thread.sleep(1000);
        
        // 전체 자원 정리
        monitor.dispose();
    }

}

// 자원 해제(정리) 메서드
interface Disposable {
    void dispose();
}

// 구독자 등록 및 해제용 구현체
class DataMonitor implements Disposable {
    private Map<String, Object> data = new HashMap<>();
    private List<Subscriber> subscribers = new ArrayList<>();

    // 구독자 추가 메서드
    public Subscription subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
        System.out.println("✓ 구독자 추가: " + subscribers.size() + "명");
        return new Subscription(this, subscriber);
    }

    // 구독자 정보 업데이트
    public void updateData(String key, Object value) {
        data.put(key, value);
        for(Subscriber sub : subscribers) {
            sub.onUpdate(key, value);
        }
    }

    // 구독 해지
    public void unsubscribe(Subscriber subscriber) {
        boolean removed = subscribers.remove(subscriber);
        if (removed) {
            System.out.println("✓ 구독자 제거: " + subscribers.size() + "명 남음");
        }
    }
    

    // 구독 리소스에 대한 자원 해제
    @Override
    public void dispose() {
        // 구독자 리스트 정리
        subscribers.clear();
        // 데이터 맵 정리
        data.clear();
        
        // 정리 완료 메시지
        System.out.println("🧹 리소스 정리 완료");
    }
}


// 구독에 대한 자원 조회
class Subscription implements Disposable {
    private DataMonitor monitor;
    private Subscriber subscriber;

    public Subscription(DataMonitor monitor, Subscriber subscriber) {
        this.monitor = monitor;
        this.subscriber = subscriber;
    }


    @Override
    public void dispose() {
        monitor.unsubscribe(subscriber);
        System.out.println("🗑️ 구독 해제");
    }
}

// 타이머 추가
class PollingTask implements Disposable {
    private Timer timer;
    private DataMonitor monitor;

    public PollingTask(DataMonitor monitor) {
        this.monitor = monitor;
    }

    // 타이머 시작
    public void start() {
        timer = new Timer();

        // 즉시 시작, 1초마다 실행처리
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               // 1초마다 랜덤 업데이트
               double randomTemp = 20 + Math.random() * 10;
               monitor.updateData("Temperature", randomTemp);
            }
        }, 0, 1000);

        System.out.println("⏰ 폴링 시작");
    }

    @Override
    public void dispose() {
        if(timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("⏰ 폴링 중지");
        }
    }
}


class Subscriber {
    private String id;

    public Subscriber(String id) {
        this.id = id;
    }

    public void onUpdate(String key, Object value) {
        System.out.println(id + " 수신: " + key + " = " + value);
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "id='" + id + '\'' +
                '}';
    }
}