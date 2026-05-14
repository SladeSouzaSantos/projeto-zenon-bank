package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class DBMain {
    void main(){
        TransactionSQLRepository sqlRepository = new TransactionSQLRepository();

        long startTime = System.nanoTime();

        List<Transaction> transactions = new TransactionIngestor().read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());
        IO.println("Iniciando adição das transações no BD...");
        transactions.forEach(sqlRepository::save);



        long endTime = System.nanoTime();

        IO.println("TransactionListRepository Demorou: " + ((endTime - startTime)/1_000_000) + "ms.");
    }
}
