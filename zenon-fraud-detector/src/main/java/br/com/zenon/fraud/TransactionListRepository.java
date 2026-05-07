package br.com.zenon.fraud;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionListRepository implements TransactionRepository {

    private final List<Transaction> transactions;

    public TransactionListRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactions = transactions;
    }

    @Override
    public Optional<Transaction> findTransactionByName(String originName){
        Optional<Transaction> optionalTransaction = transactions.stream()
                .filter(transaction -> transaction.origin().name().equals(originName))
                .findFirst();

        optionalTransaction.ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para " + originName));

        return optionalTransaction;
    }
}
