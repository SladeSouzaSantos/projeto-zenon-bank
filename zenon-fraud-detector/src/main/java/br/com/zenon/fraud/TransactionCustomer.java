package br.com.zenon.fraud;

import java.math.BigDecimal;

public record TransactionCustomer(String name, BigDecimal oldBalance, BigDecimal newBalance) {
    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", oldBalance=" + oldBalance +
                ", newBalance=" + newBalance +
                '}';
    }
}
