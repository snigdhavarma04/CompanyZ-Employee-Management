package companyz.model;

import java.time.LocalDate;

public class PayrollRecord {
    private int recordId;
    private double grossPay;
    private LocalDate payDate;

    public PayrollRecord(int recordId, double grossPay, LocalDate payDate) {
        this.recordId = recordId;
        this.grossPay = grossPay;
        this.payDate = payDate;
    }

    // Getters
    public int getRecordId() { return recordId; }
    public double getGrossPay() { return grossPay; }
    public LocalDate getPayDate() { return payDate; }
}