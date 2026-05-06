package br.com.zenon;

import br.com.zenon.fraud.Transaction;
import br.com.zenon.fraud.TransactionCustomer;
import br.com.zenon.fraud.TransactionIngestor;
import br.com.zenon.fraud.TransactionType;

import java.math.BigDecimal;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Transaction transaction1 = new Transaction(1L, TransactionType.PAYMENT, new BigDecimal("9839.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false, false);

        Transaction transaction2 = new Transaction(743L, TransactionType.CASH_OUT, new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true, false);

        IO.println(transaction1);
        IO.println(transaction2);

        IO.println("---------------------------------------------------------");

        List<Transaction> transactions = new TransactionIngestor().read("data/PS_20174392719_1491204439457_log.csv");
        transactions.stream().limit(10).forEach(IO::println);

    }
}
