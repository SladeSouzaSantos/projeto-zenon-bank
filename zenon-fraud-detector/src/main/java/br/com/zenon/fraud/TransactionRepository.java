package br.com.zenon.fraud;

import java.util.Optional;

public interface TransactionRepository {
    Optional<Transaction> findTransactionByName(String originName);
}
