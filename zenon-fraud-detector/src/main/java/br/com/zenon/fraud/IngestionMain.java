package br.com.zenon.fraud;

import java.util.List;

public class IngestionMain {
    void main(){
        TransactionSQLRepository sqlRepository = new TransactionSQLRepository();

        long startTime = System.nanoTime();

        //List<Transaction> transactions = new TransactionIngestor().read("data/PS_20174392719_1491204439457_log.csv");

        //IO.println(transactions.size());
        IO.println("Iniciando adição das transações no BD...");
        //sqlRepository.saveAll(transactions);

        /*new EfficientTransactionIngestor().readAsStream(
                "data/PS_20174392719_1491204439457_log.csv",
                sqlRepository::save
        );*/

        new EfficientTransactionIngestor().readAsBatch(
                "data/PS_20174392719_1491204439457_log.csv",
                sqlRepository::saveAll
        );

        long endTime = System.nanoTime();

        IO.println("Tempo de ingestão no BD (ms): " + ((endTime - startTime)/1_000_000) + "ms.");
    }
}
