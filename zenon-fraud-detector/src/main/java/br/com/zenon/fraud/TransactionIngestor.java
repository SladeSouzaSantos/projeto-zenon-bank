package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TransactionIngestor {

    public static final int FRAUD_LIMIT = 100_000;

    public List<Transaction> read(String fileName) {
        Path path = Paths.get(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .limit(FRAUD_LIMIT)
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        }
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] transactionFields = line.split(",");

            validateFields(transactionFields);

            int split = Integer.parseInt(transactionFields[0]);
            TransactionType type = TransactionType.valueOf(transactionFields[1]);
            BigDecimal amount = new BigDecimal(transactionFields[2]);
            TransactionCustomer origin = new TransactionCustomer(transactionFields[3], new BigDecimal(transactionFields[4]), new BigDecimal(transactionFields[5]));
            TransactionCustomer destination = new TransactionCustomer(transactionFields[6], new BigDecimal(transactionFields[7]), new BigDecimal(transactionFields[8]));
            boolean isFraud = transactionFields[9].equals("1");
            boolean isFlaggedFraud = transactionFields[10].equals("1");

            return Optional.of(new Transaction(split, type, amount, origin, destination, isFraud, isFlaggedFraud));
        } catch (Exception e) {
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }

    private static void validateFields(String[] transactionFields) {
        List<String> errors = new ArrayList<>();

        if (Arrays.stream(TransactionType.values()).noneMatch(t -> t.name().equals(transactionFields[1])))
            errors.add("O TransactionType." + transactionFields[1] + " não existe. Favor informar uma das opções existentes "+Arrays.toString(TransactionType.values())+".");
        if(transactionFields[2] == null || transactionFields[2].trim().isEmpty())
            errors.add("O valor de amount não pode ser nulo e nem vázio.");
        if((transactionFields[4] == null || transactionFields[4].trim().isEmpty()) || (transactionFields[7] == null || transactionFields[7].trim().isEmpty()))
            errors.add("O valor de oldBalance não pode ser nulo e nem vázio.");
        if((transactionFields[5] == null || transactionFields[5].trim().isEmpty()) || (transactionFields[8] == null || transactionFields[8].trim().isEmpty()))
            errors.add("O valor de newBalance não pode ser nulo e nem vázio.");
        if(transactionFields[9] == null || transactionFields[9].trim().isEmpty())
            errors.add("O valor de isFraud não pode ser nulo e nem vázio.");
        if(transactionFields[10] == null || transactionFields[10].trim().isEmpty())
            errors.add("O valor de isFlaggedFraud não pode ser nulo e nem vázio.");

        if (!errors.isEmpty()) {
            if(errors.size() > 1)
                throw new IllegalArgumentException("Erros encontrados: " + String.join(", ", errors));

            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
