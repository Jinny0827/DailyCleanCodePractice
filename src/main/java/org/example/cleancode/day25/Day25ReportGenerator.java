package org.example.cleancode.day25;


/**
 * Day 25: 보고서 생성 시스템
 *
 * 문제점:
 * - 보고서 생성 로직이 클라이언트 코드에 노출됨
 * - 타입별 생성 조건이 복잡하게 분기됨
 * - 새로운 보고서 타입 추가 시 여러 곳 수정 필요
 * - 생성 과정의 검증 로직이 중복됨
 */
public class Day25ReportGenerator {
    public static void main(String[] args) {
        System.out.println("=== 기본 보고서 생성 ===");
        
        // 판매 보고서(기본 값 사용)
        Report salesReport = ReportFactory.createReport(
                ReportType.SALES,
                ReportFormat.PDF
        );
        salesReport.generate();

        System.out.println();


        // 판매 보고서(Excel)
        Report salesExcel = ReportFactory.createReport(
                ReportType.SALES,
                ReportFormat.EXCEL
        );
        salesExcel.generate();

        System.out.println();
        
        // 재고 보고서
        Report inventoryReport = ReportFactory.createReport(
            ReportType.INVENTORY,
                ReportFormat.PDF
        );
        inventoryReport.generate();

        System.out.println();

        
        // 사용자 활동 보고서
        Report activityReport = ReportFactory.createReport(
                ReportType.USER_ACTIVITY,
                ReportFormat.PDF
        );
        activityReport.generate();

        System.out.println();


        // 커스텀 재고 보고서
        String[] customFields = {"item_code", "quantity", "warehouse"};
        InventoryReport customInventoryReport = ReportFactory.createCustomInventoryReport(
                ReportFormat.CSV,
                customFields,
                5
        );
        customInventoryReport.generate();

    }
}


interface Report {
    void generate();
}

enum ReportType {
    SALES,
    INVENTORY,
    USER_ACTIVITY
}

enum ReportFormat {
    PDF,
    EXCEL,
    CSV
}

class SalesReport implements Report {
    private final ReportFormat format;
    private final String[] fields;
    private final String chartType;
    private final boolean includeSummary;

    public SalesReport(ReportFormat format, String[] fields, String chartType, boolean includeSummary) {
        this.format = format;
        this.fields = fields;
        this.chartType = chartType;
        this.includeSummary = includeSummary;
    }

    public void generate() {
        System.out.println("=== 판매 보고서 (" + format + ") ===");
        System.out.println("필드: " + String.join(", ", fields));
        if (chartType != null) {
            System.out.println("차트: " + chartType);
        }
        if (includeSummary) {
            System.out.println("요약 포함");
        }
    }
}

class InventoryReport implements Report {
    private final ReportFormat format;
    private final String[] fields;
    private final int warningThreshold;

    public InventoryReport(ReportFormat format, String[] fields, int warningThreshold) {
        this.format = format;
        this.fields = fields;
        this.warningThreshold = warningThreshold;
    }

    public void generate() {
        System.out.println("=== 재고 보고서 (" + format + ") ===");
        System.out.println("필드: " + String.join(", ", fields));
        System.out.println("경고 임계값: " + warningThreshold);
    }
}

class UserActivityReport implements Report {
    private final ReportFormat format;
    private final String period;
    private final boolean includeCharts;

    public UserActivityReport(ReportFormat format, String period, boolean includeCharts) {
        this.format = format;
        this.period = period;
        this.includeCharts = includeCharts;
    }

    public void generate() {
        System.out.println("=== 사용자 활동 보고서 (" + format + ") ===");
        System.out.println("기간: " + period);
        if (includeCharts) {
            System.out.println("차트 포함");
        }
    }
}

class ReportFactory {
    public static Report createReport(ReportType type, ReportFormat format) {
        switch (type) {
            case SALES:
                return createSalesReport(format);
            case INVENTORY:
                return createInventoryReport(format);
            case USER_ACTIVITY:
                return createUserActivityReport(format);
            default:
                throw new IllegalArgumentException("지원하지 않는 보고서 타입: " + type);
        }
    }

    // 판매 보고서 작성
    private static SalesReport createSalesReport(ReportFormat format) {
        String[] defaultFields = {"product_id", "quantity", "revenue"};
        String chartType = format == ReportFormat.PDF ? "BAR" : null;
        boolean includeSummary = format == ReportFormat.EXCEL;

        return new SalesReport(format, defaultFields, chartType, includeSummary);
    }

    // 재고 보고서 생성
    private static InventoryReport createInventoryReport(ReportFormat format) {
        String[] defaultFields = {"item_name", "stock_level", "location"};
        int defaultThreshold = 10;

        return new InventoryReport(format, defaultFields, defaultThreshold);
    }

    // 사용자 활동 보고서 생성
    private static UserActivityReport createUserActivityReport(ReportFormat format) {
        String defaultPeriod = "MONTHLY";
        boolean includeCharts = format == ReportFormat.PDF;

        return new UserActivityReport(format, defaultPeriod, includeCharts);
    }
    
    // 커스텀 판매 보고서 생성
    private static SalesReport createCustomSalesReport( ReportFormat format,
                                                        String[] fields,
                                                        String chartType,
                                                        boolean includeSummary) {
        validateFields(fields);
        return new SalesReport(format, fields, chartType, includeSummary);
    }
    
    // 커스텀 재고 보고서 생성
    public static InventoryReport createCustomInventoryReport(
            ReportFormat format,
            String[] fields,
            int warningThreshold) {

        validateFields(fields);
        validateThreshold(warningThreshold);
        return new InventoryReport(format, fields, warningThreshold);
    }
    
    // 유효성 검증 메서드들
    private static void validateFields(String[] fields) {
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("필드는 최소 1개 이상이어야 합니다");
        }
    }

    private static void validateThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("임계값은 0 이상이어야 합니다");
        }
    }
    

}