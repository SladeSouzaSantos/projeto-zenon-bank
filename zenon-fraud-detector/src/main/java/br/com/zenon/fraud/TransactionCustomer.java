package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.Objects;

public record TransactionCustomer(String name, BigDecimal oldBalance, BigDecimal newBalance) {

    public TransactionCustomer{
        Objects.requireNonNull(name);
        Objects.requireNonNull(oldBalance);
        Objects.requireNonNull(newBalance);

        if(name.isBlank()) throw new IllegalArgumentException("O name não pode ser vázio. Valor aplicado: " + name);
        if(oldBalance.signum() < 0) throw new IllegalArgumentException("O valor de oldBalance deve ser positivo. Valor aplicado: " + oldBalance);
        if(newBalance.signum() < 0) throw new IllegalArgumentException("O valor de newBalance deve ser positivo. Valor aplicado: " + newBalance);
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", oldBalance=" + oldBalance +
                ", newBalance=" + newBalance +
                '}';
    }
}
