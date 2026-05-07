package br.com.zenon.fraud;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransactionMapRepository implements TransactionRepository  {

    private final Map<String, Transaction> transactionByOriginName;

    public TransactionMapRepository(List<Transaction> transactions) {
        Objects.requireNonNull(transactions);
        this.transactionByOriginName = transactions.stream()
                .collect(Collectors.toMap(transaction -> transaction.origin().name(), Function.identity()));
    }

    @Override
    public Optional<Transaction> findTransactionByName(String originName) {
        Optional<Transaction> optionalTransaction =  Optional.ofNullable(transactionByOriginName.get(originName));

        optionalTransaction.ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para " + originName));

        return optionalTransaction;
    }
}
