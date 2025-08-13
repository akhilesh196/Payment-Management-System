package miniproject1.paymentmanagementsystem.service;

import miniproject1.paymentmanagementsystem.model.Payment;
import miniproject1.paymentmanagementsystem.repository.PaymentRepository;
import miniproject1.paymentmanagementsystem.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public ReportService() throws Exception {
        this.paymentRepository = new PaymentRepository();
        this.userRepository = new UserRepository();
    }

    /**
     * Generate monthly report for a specific month and year
     */
    public MonthlyReport generateMonthlyReport(int year, int month) throws SQLException {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        return createMonthlyReport(payments, yearMonth);
    }

    /**
     * Generate quarterly report for a specific quarter and year
     */
    public QuarterlyReport generateQuarterlyReport(int year, int quarter) throws SQLException {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }

        // Calculate quarter date range
        int startMonth = (quarter - 1) * 3 + 1;
        YearMonth startYearMonth = YearMonth.of(year, startMonth);
        YearMonth endYearMonth = startYearMonth.plusMonths(2);

        LocalDate startDate = startYearMonth.atDay(1);
        LocalDate endDate = endYearMonth.atEndOfMonth();

        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        return createQuarterlyReport(payments, year, quarter, startDate, endDate);
    }

    /**
     * Generate current month report
     */
    public MonthlyReport generateCurrentMonthReport() throws SQLException {
        YearMonth currentMonth = YearMonth.now();
        return generateMonthlyReport(currentMonth.getYear(), currentMonth.getMonthValue());
    }

    /**
     * Generate current quarter report
     */
    public QuarterlyReport generateCurrentQuarterReport() throws SQLException {
        LocalDate today = LocalDate.now();
        int quarter = (today.getMonthValue() - 1) / 3 + 1;
        return generateQuarterlyReport(today.getYear(), quarter);
    }

    private List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(payment -> {
                    LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
                    return !paymentDate.isBefore(startDate) && !paymentDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    private MonthlyReport createMonthlyReport(List<Payment> payments, YearMonth yearMonth) {
        MonthlyReport report = new MonthlyReport();
        report.setYear(yearMonth.getYear());
        report.setMonth(yearMonth.getMonthValue());
        report.setMonthName(yearMonth.getMonth().name());

        // Calculate totals
        report.setTotalPayments(payments.size());
        report.setTotalAmount(calculateTotalAmount(payments));

        // Calculate by status
        Map<String, Integer> paymentsByStatus = new HashMap<>();
        Map<String, BigDecimal> amountsByStatus = new HashMap<>();

        for (Payment payment : payments) {
            String status = payment.getStatus().getStatusName();
            paymentsByStatus.merge(status, 1, Integer::sum);
            amountsByStatus.merge(status, payment.getAmount(), BigDecimal::add);
        }

        report.setPaymentsByStatus(paymentsByStatus);
        report.setAmountsByStatus(amountsByStatus);

        // Calculate by type
        Map<String, Integer> paymentsByType = new HashMap<>();
        Map<String, BigDecimal> amountsByType = new HashMap<>();

        for (Payment payment : payments) {
            String type = payment.getType();
            paymentsByType.merge(type, 1, Integer::sum);
            amountsByType.merge(type, payment.getAmount(), BigDecimal::add);
        }

        report.setPaymentsByType(paymentsByType);
        report.setAmountsByType(amountsByType);

        // Calculate by category
        Map<String, Integer> paymentsByCategory = new HashMap<>();
        Map<String, BigDecimal> amountsByCategory = new HashMap<>();

        for (Payment payment : payments) {
            String category = payment.getCategory().getCategoryName();
            paymentsByCategory.merge(category, 1, Integer::sum);
            amountsByCategory.merge(category, payment.getAmount(), BigDecimal::add);
        }

        report.setPaymentsByCategory(paymentsByCategory);
        report.setAmountsByCategory(amountsByCategory);

        return report;
    }

    private QuarterlyReport createQuarterlyReport(List<Payment> payments, int year, int quarter,
                                                LocalDate startDate, LocalDate endDate) {
        QuarterlyReport report = new QuarterlyReport();
        report.setYear(year);
        report.setQuarter(quarter);
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Calculate totals
        report.setTotalPayments(payments.size());
        report.setTotalAmount(calculateTotalAmount(payments));

        // Group payments by month within the quarter
        Map<YearMonth, List<Payment>> paymentsByMonth = payments.stream()
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getPaymentDate())));

        Map<String, MonthlyReport> monthlyReports = new HashMap<>();
        for (Map.Entry<YearMonth, List<Payment>> entry : paymentsByMonth.entrySet()) {
            YearMonth month = entry.getKey();
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyReports.put(monthKey, createMonthlyReport(entry.getValue(), month));
        }

        report.setMonthlyBreakdown(monthlyReports);

        // Calculate by status for entire quarter
        Map<String, Integer> paymentsByStatus = new HashMap<>();
        Map<String, BigDecimal> amountsByStatus = new HashMap<>();

        for (Payment payment : payments) {
            String status = payment.getStatus().getStatusName();
            paymentsByStatus.merge(status, 1, Integer::sum);
            amountsByStatus.merge(status, payment.getAmount(), BigDecimal::add);
        }

        report.setPaymentsByStatus(paymentsByStatus);
        report.setAmountsByStatus(amountsByStatus);

        // Calculate by type for entire quarter
        Map<String, Integer> paymentsByType = new HashMap<>();
        Map<String, BigDecimal> amountsByType = new HashMap<>();

        for (Payment payment : payments) {
            String type = payment.getType();
            paymentsByType.merge(type, 1, Integer::sum);
            amountsByType.merge(type, payment.getAmount(), BigDecimal::add);
        }

        report.setPaymentsByType(paymentsByType);
        report.setAmountsByType(amountsByType);

        return report;
    }

    private BigDecimal calculateTotalAmount(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Inner classes for report data structures
    public static class MonthlyReport {
        private int year;
        private int month;
        private String monthName;
        private int totalPayments;
        private BigDecimal totalAmount;
        private Map<String, Integer> paymentsByStatus;
        private Map<String, BigDecimal> amountsByStatus;
        private Map<String, Integer> paymentsByType;
        private Map<String, BigDecimal> amountsByType;
        private Map<String, Integer> paymentsByCategory;
        private Map<String, BigDecimal> amountsByCategory;

        // Getters and setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }

        public String getMonthName() { return monthName; }
        public void setMonthName(String monthName) { this.monthName = monthName; }

        public int getTotalPayments() { return totalPayments; }
        public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Map<String, Integer> getPaymentsByStatus() { return paymentsByStatus; }
        public void setPaymentsByStatus(Map<String, Integer> paymentsByStatus) { this.paymentsByStatus = paymentsByStatus; }

        public Map<String, BigDecimal> getAmountsByStatus() { return amountsByStatus; }
        public void setAmountsByStatus(Map<String, BigDecimal> amountsByStatus) { this.amountsByStatus = amountsByStatus; }

        public Map<String, Integer> getPaymentsByType() { return paymentsByType; }
        public void setPaymentsByType(Map<String, Integer> paymentsByType) { this.paymentsByType = paymentsByType; }

        public Map<String, BigDecimal> getAmountsByType() { return amountsByType; }
        public void setAmountsByType(Map<String, BigDecimal> amountsByType) { this.amountsByType = amountsByType; }

        public Map<String, Integer> getPaymentsByCategory() { return paymentsByCategory; }
        public void setPaymentsByCategory(Map<String, Integer> paymentsByCategory) { this.paymentsByCategory = paymentsByCategory; }

        public Map<String, BigDecimal> getAmountsByCategory() { return amountsByCategory; }
        public void setAmountsByCategory(Map<String, BigDecimal> amountsByCategory) { this.amountsByCategory = amountsByCategory; }
    }

    public static class QuarterlyReport {
        private int year;
        private int quarter;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalPayments;
        private BigDecimal totalAmount;
        private Map<String, Integer> paymentsByStatus;
        private Map<String, BigDecimal> amountsByStatus;
        private Map<String, Integer> paymentsByType;
        private Map<String, BigDecimal> amountsByType;
        private Map<String, MonthlyReport> monthlyBreakdown;

        // Getters and setters
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public int getQuarter() { return quarter; }
        public void setQuarter(int quarter) { this.quarter = quarter; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public int getTotalPayments() { return totalPayments; }
        public void setTotalPayments(int totalPayments) { this.totalPayments = totalPayments; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Map<String, Integer> getPaymentsByStatus() { return paymentsByStatus; }
        public void setPaymentsByStatus(Map<String, Integer> paymentsByStatus) { this.paymentsByStatus = paymentsByStatus; }

        public Map<String, BigDecimal> getAmountsByStatus() { return amountsByStatus; }
        public void setAmountsByStatus(Map<String, BigDecimal> amountsByStatus) { this.amountsByStatus = amountsByStatus; }

        public Map<String, Integer> getPaymentsByType() { return paymentsByType; }
        public void setPaymentsByType(Map<String, Integer> paymentsByType) { this.paymentsByType = paymentsByType; }

        public Map<String, BigDecimal> getAmountsByType() { return amountsByType; }
        public void setAmountsByType(Map<String, BigDecimal> amountsByType) { this.amountsByType = amountsByType; }

        public Map<String, MonthlyReport> getMonthlyBreakdown() { return monthlyBreakdown; }
        public void setMonthlyBreakdown(Map<String, MonthlyReport> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
    }
}
