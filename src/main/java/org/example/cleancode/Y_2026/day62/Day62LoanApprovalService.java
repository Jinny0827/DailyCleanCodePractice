package org.example.cleancode.Y_2026.day62;

/**
 * Day 62: 비즈니스 규칙 객체화 (Business Rule Extraction)
 *
 * 각 검증 로직을 독립된 Rule 클래스로 추출
 * LoanRule 인터페이스 정의
 * Rule들을 컬렉션으로 관리하여 반복 처리
 * 이자율 계산도 별도 전략으로 분리
 */

public class Day62LoanApprovalService {

    public String approveLoan(Customer customer, int loanAmount, int months) {
        // 대출 승인 로직
        if (customer.getCreditScore() < 600) {
            return "REJECTED: Low credit score";
        }

        if (customer.getAge() < 20 || customer.getAge() > 65) {
            return "REJECTED: Age not in range";
        }

        if (customer.getAnnualIncome() < loanAmount * 0.3) {
            return "REJECTED: Insufficient income";
        }

        if (loanAmount > 50000000 && customer.getEmploymentYears() < 3) {
            return "REJECTED: Insufficient employment history for large loan";
        }

        if (customer.hasActiveDefaultLoans()) {
            return "REJECTED: Active default loans exist";
        }

        if (months > 120) {
            return "REJECTED: Loan period too long";
        }

        // 이자율 계산
        double interestRate = 4.5;
        if (customer.getCreditScore() >= 800) {
            interestRate = 3.2;
        } else if (customer.getCreditScore() >= 700) {
            interestRate = 3.8;
        }

        return "APPROVED: Interest rate " + interestRate + "%";
    }
}

class Customer {
    private String name;
    private int age;
    private int creditScore;
    private int annualIncome;
    private int employmentYears;
    private boolean hasActiveDefaultLoans;

    public Customer(String name, int age, int creditScore,
                    int annualIncome, int employmentYears,
                    boolean hasActiveDefaultLoans) {
        this.name = name;
        this.age = age;
        this.creditScore = creditScore;
        this.annualIncome = annualIncome;
        this.employmentYears = employmentYears;
        this.hasActiveDefaultLoans = hasActiveDefaultLoans;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public int getAnnualIncome() {
        return annualIncome;
    }

    public int getEmploymentYears() {
        return employmentYears;
    }

    public boolean isHasActiveDefaultLoans() {
        return hasActiveDefaultLoans;
    }
}
