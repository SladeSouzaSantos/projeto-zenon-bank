package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {

    public static final int FRAUD_LIMIT = 10_000;
    public static final int LINE_BATCH_SIZE = 2_500;

    private final Semaphore dbPermits = new Semaphore(10);

    public void readAsStream(String fileName, Consumer<Transaction> consumer) {
        Path path = Paths.get(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            lines
                .skip(1)
                .limit(FRAUD_LIMIT)
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(consumer);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        }
    }

    public void readAsBatch(String fileName, Consumer<List<Transaction>> batchConsumer) {
        Path path = Paths.get(fileName);

        // 1. Executor fora do try-with-resources para não causar deadlock
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try (Stream<String> lines = Files.lines(path).skip(1)) {
            Iterator<String> iterator = lines.iterator();

            List<String> lineBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while (iterator.hasNext()){
                String line = iterator.next();
                lineBatch.add(line);

                if(lineBatch.size() >= LINE_BATCH_SIZE){
                    IO.println("Lendo lote e aguardando liberação do banco...");
                    final List<String> currentLineBatch = List.copyOf(lineBatch);
                    lineBatch.clear();

                    // 2. O PULO DO GATO: O loop principal PARA AQUI até ter vaga no banco.
                    // Isso impede que a memória RAM lote com milhões de linhas lidas à toa.
                    dbPermits.acquire();

                    executor.submit(() -> {
                        try {
                            executeBactch(currentLineBatch, batchConsumer);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }
            }

            if(!lineBatch.isEmpty()){
                IO.println("Executando batch final injestor...");
                final List<String> currentLineBatch = List.copyOf(lineBatch);

                dbPermits.acquire();
                executor.submit(() -> {
                    try {
                        executeBactch(currentLineBatch, batchConsumer);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        } finally {
            // 3. Encerramento seguro das threads após ler o arquivo inteiro
            executor.shutdown();
            try {
                // Aguarda até 1 hora para o banco terminar de processar os últimos lotes
                executor.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void executeBactch(List<String> lineBatch, Consumer<List<Transaction>> batchConsumer) {
        try {
            List<Transaction> transactionBatch = lineBatch
                    .stream()
                    .map(this::parseTransaction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            // Envia os dados para o MySQL
            batchConsumer.accept(transactionBatch);

        } finally {
            // 4. O MAIS IMPORTANTE: Avisa a thread principal que terminou e libera a vaga
            dbPermits.release();
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
