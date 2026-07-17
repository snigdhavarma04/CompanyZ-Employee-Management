package companyz.model;

public interface Payable {
    /**
     * Calculates the payment amount for the current period.
     * @return double representing the calculated pay.
     */
    double calculatePay();
}