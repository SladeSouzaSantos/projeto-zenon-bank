package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.Objects;

public record Transaction(
        int step,
        TransactionType type,
        BigDecimal amount,
        TransactionCustomer origin,
        TransactionCustomer destination,
        boolean isFraud,
        boolean isFlaggedFraud
        ) {

    public Transaction {
        Objects.requireNonNull(type);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(origin);
        Objects.requireNonNull(destination);

        if(step <= 0) throw new IllegalArgumentException("O valor de step deve ser maior que zero. Valor aplicado: " + step);
        if(amount.signum() < 0) throw new IllegalArgumentException("O valor de amount deve ser positivo. Valor aplicado: " + amount);
    }

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

