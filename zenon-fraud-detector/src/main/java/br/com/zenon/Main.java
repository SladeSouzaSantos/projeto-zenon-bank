package br.com.zenon;

import br.com.zenon.fraud.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static br.com.zenon.fraud.TransactionReport.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static String language;

    void main(String[] args) {
        language = args.length > 0 ? args[0] : "pt";
        Locale locale = switch (language.toLowerCase()) {
            case "en" -> Locale.US;
            default -> Locale.of("pt", "BR");
        };

        List<Transaction> transactions = new TransactionIngestor().read("data/PS_20174392719_1491204439457_log.csv");

        runEfficientNioReport(locale);

        showManualInstantiation();

        previewTransactionData(transactions);

        testIngestionWithInvalidData();

        analyzeFraudStatistics(transactions);

        compareRepositoryPerformance(transactions);

    }

    private static void runEfficientNioReport(Locale locale) {
        var context = ReportContext.of(locale);

        TransactionReport transactionReport = new TransactionReport();
        Statistics statistics = transactionReport.generateReport("data/PS_20174392719_1491204439457_log.csv");

        String formattedTotalTransactions = context.integer().format(statistics.totalTransactions());
        String formattedTotalFrauds = context.integer().format(statistics.totalFrauds());
        String formattedTotalAmount = context.currency().format(statistics.totalAmount());

        String msgTotalTransactions = context.bundle().getString("label.total.transactions");
        String msgTotalFrauds = context.bundle().getString("label.total.frauds");
        String msgTotalAmount = context.bundle().getString("label.total.amount");

        IO.println("""
                %s: %s
                %s: %s,
                %s: %s
                """.formatted(
                    msgTotalTransactions, formattedTotalTransactions,
                    msgTotalFrauds, formattedTotalFrauds,
                    msgTotalAmount, formattedTotalAmount
                )
        );
    }

    private static void compareRepositoryPerformance(List<Transaction> transactions) {
        TransactionRepository transactionRepository = new TransactionListRepository(transactions);

        String notFoundOriginNameExample = "C12345";
        transactionRepository.findTransactionByName(notFoundOriginNameExample);

        String foundOriginNameExample = "C1231006815";
        transactionRepository.findTransactionByName(foundOriginNameExample);

        String lastOriginNameExample = "C1868032458";

        long startTime = System.nanoTime();

        transactionRepository.findTransactionByName(lastOriginNameExample);

        long endTime = System.nanoTime();

        IO.println("TransactionListRepository Demorou: " + ((endTime - startTime)/1_000_000) + "ms.");

        transactionRepository = new TransactionMapRepository(transactions);

        startTime = System.nanoTime();

        transactionRepository.findTransactionByName(lastOriginNameExample);

        endTime = System.nanoTime();

        IO.println("TransactionMapRepository Demorou: " + ((endTime - startTime)/1_000_000) + "ms.");

        IO.println("---------------------------------------------------------");
    }

    private static void analyzeFraudStatistics(List<Transaction> transactions) {
        FraudAnalyzer fraudAnalyzer = new FraudAnalyzer(transactions);

        long totalFrauds = fraudAnalyzer.countFrauds();
        IO.println("Total de fraudes: " + totalFrauds);

        IO.println("\nTop 3 fraudes de maior valor:");
        List<BigDecimal> highestFraudAmounts = fraudAnalyzer.findHighestValueFraudAmounts(3);
        highestFraudAmounts.forEach(amount -> IO.println("%.2f".formatted(amount)));

        IO.println("\nTop 5 clientes suspeitos:");
        List<String> suspiciousClients = fraudAnalyzer.findTopSuspiciousClients(5);
        suspiciousClients.forEach(IO::println);

        IO.println("\nPrejuízo total causado pelas fraudes:");
        BigDecimal totalFraudLoss = fraudAnalyzer.calculateTotalFraudLoss();
        IO.println("Prejuízo total: " + totalFraudLoss);

        IO.println("\nQuantidade de fraudes por tipo:");
        Map<TransactionType, Long> fraudCountByType = fraudAnalyzer.countFraudsByType();
        IO.println("Fraudes por tipo:");
        fraudCountByType.forEach((type, count) -> IO.println("- %s:  %d".formatted(type, count)));

        IO.println("---------------------------------------------------------");
    }

    private static void testIngestionWithInvalidData() {
        List<Transaction> transactionsFileWithErrors = new TransactionIngestor().read("data/paysim_with_bad_data.csv");
        transactionsFileWithErrors.forEach(IO::println);

        IO.println("---------------------------------------------------------");
    }

    private static void previewTransactionData(List<Transaction> transactions) {

        transactions.stream().limit(10).forEach(IO::println);

        IO.println("---------------------------------------------------------");
    }

    private static void showManualInstantiation() {
        Transaction transaction1 = new Transaction(1, TransactionType.PAYMENT, new BigDecimal("9839.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false, false);

        Transaction transaction2 = new Transaction(743, TransactionType.CASH_OUT, new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true, false);

        IO.println(transaction1);
        IO.println(transaction2);

        IO.println("---------------------------------------------------------");
    }
}
