package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TransactionReport {

    private record ReportTransaction(BigDecimal amount, boolean isFraud){
    }

    public record Statistics(long totalTransactions, long totalFrauds, BigDecimal totalAmount){
        private final static Statistics ZERO = new Statistics(0, 0, BigDecimal.ZERO);

        private Statistics addReportTransaction(ReportTransaction reportTransaction) {
            return new Statistics(
                    totalTransactions + 1,
                    totalFrauds + (reportTransaction.isFraud ? 1 : 0),
                    totalAmount.add(reportTransaction.amount));
        }

        private Statistics addStatistics(Statistics other){
            return new Statistics(totalTransactions+other.totalTransactions,
                    totalFrauds+other.totalFrauds,
                    totalAmount.add(other.totalAmount));
        }
    }

    public Statistics generateReport(String fileName){
        Path path = Paths.get(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .skip(1)
                    .map(this::parseReportTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .reduce(Statistics.ZERO, Statistics::addReportTransaction, Statistics::addStatistics);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        }
    }

    private Optional<ReportTransaction> parseReportTransaction(String line) {
        try {
            String[] transactionFields = line.split(",");

            validateFields(transactionFields);

            BigDecimal amount = new BigDecimal(transactionFields[2]);
            boolean isFraud = transactionFields[9].equals("1");

            return Optional.of(new ReportTransaction(amount, isFraud));
        } catch (Exception e) {
            System.err.println("Erro ao fazer parse: " + line + " | " + e);
            return Optional.empty();
        }
    }

    private static void validateFields(String[] transactionFields) {
        List<String> errors = new ArrayList<>();

        if(transactionFields[2] == null || transactionFields[2].trim().isEmpty())
            errors.add("O valor de amount não pode ser nulo e nem vázio.");
        if(transactionFields[9] == null || transactionFields[9].trim().isEmpty())
            errors.add("O valor de isFraud não pode ser nulo e nem vázio.");

        if (!errors.isEmpty()) {
            if(errors.size() > 1)
                throw new IllegalArgumentException("Erros encontrados: " + String.join(", ", errors));

            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
