package org.example.cleancode.Y_2026.day62;

import java.util.List;

/**
 * Day 62: 비즈니스 규칙 객체화 (Business Rule Extraction)
 *
 * 각 검증 로직을 독립된 Rule 클래스로 추출
 * LoanRule 인터페이스 정의
 * Rule들을 컬렉션으로 관리하여 반복 처리
 * 이자율 계산도 별도 전략으로 분리
 */

public class Day62LoanApprovalService {

    private final List<LoanRule> rules;
    private final InterestRateCalculator rateCalculator;

    public Day62LoanApprovalService() {
        this.rules = List.of(
                new CreditScoreRule(),
                new AgeRangeRule(),
                new IncomeRule(),
                new LargeLoanEmploymentRule(),
                new DefaultLoanRule(),
                new LoanPeriodRule()
        );
        this.rateCalculator = new CreditScoreBasedRateCalculator();
    }

    public String approveLoan(Customer customer, int loanAmount, int months) {
        for(LoanRule rule : rules) {
            String result = rule.validate(customer, loanAmount, months);

            if(result != null) {
                return result;
            }
        }

        // 이자율 계산
        double interestRate = rateCalculator.calculate(customer.getCreditScore());

        return "APPROVED: Interest rate " + interestRate + "%";
    }
}

// ========== 이자율 계산 전략 인터페이스 ==========
interface InterestRateCalculator {
    double calculate(int creditScore);
}


// 이자율 계산 전략 구현체
class CreditScoreBasedRateCalculator implements InterestRateCalculator {
    @Override
    public double calculate(int creditScore) {
        double interestRate = 4.5;

        if (creditScore >= 800) {  // customer 대신 파라미터 사용
            interestRate = 3.2;
        } else if (creditScore >= 700) {
            interestRate = 3.8;
        }

        return interestRate;
    }
}



// ========== 대출 규칙 인터페이스 ==========
interface LoanRule {
    String validate(Customer customer, int loanAmount, int months);
}

// ========== 대출 규칙 Rule 구현체들 ==========
class CreditScoreRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return customer.getCreditScore() < 600 ? "REJECTED: Low credit score" : null;
    }
}

class AgeRangeRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return customer.getAge() < 20 || customer.getAge() > 65 ? "REJECTED: Age not in range" : null;
    }
}

class IncomeRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return customer.getAnnualIncome() < loanAmount * 0.3 ? "REJECTED: Insufficient income" : null;
    }
}

class LargeLoanEmploymentRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return loanAmount > 50000000 && customer.getEmploymentYears() < 3 ?
                "REJECTED: Insufficient employment history for large loan" : null;
    }
}

class DefaultLoanRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return customer.hasActiveDefaultLoans() ? "REJECTED: Active default loans exist" : null;
    }
}

class LoanPeriodRule implements LoanRule {
    @Override
    public String validate(Customer customer, int loanAmount, int months) {
        return months > 120 ? "REJECTED: Loan period too long" : null;
    }
}

// ========== Customer 클래스 ==========
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

    public String getName() { return name; }
    public int getAge() { return age; }
    public int getCreditScore() { return creditScore; }
    public int getAnnualIncome() { return annualIncome; }
    public int getEmploymentYears() { return employmentYears; }
    public boolean hasActiveDefaultLoans() { return hasActiveDefaultLoans; }
}
