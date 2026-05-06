package br.com.zenon.fraud;

import java.math.BigDecimal;

public record Transaction(
        Long step,
        TransactionType type,
        BigDecimal amount,
        TransactionCustomer origin,
        TransactionCustomer destination,
        boolean isFraud,
        boolean isFlaggedFraud
        ) {
    @Override
    public String toString() {
        return "Transaction{" +
                "step=" + step +
                ", type=" + type +
                ", amount=" + amount +
                ", origin=" + origin +
                ", destination=" + destination +
                ", isFraud=" + isFraud +
                ", isFlaggedFraud=" + isFlaggedFraud +
                '}';
    }
}

