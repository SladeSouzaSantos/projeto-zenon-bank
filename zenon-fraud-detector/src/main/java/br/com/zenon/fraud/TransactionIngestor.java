package br.com.zenon.fraud;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class TransactionIngestor {

    public List<Transaction> read(String fileName) {
        Path path = Paths.get(fileName);

        try (Stream<String> lines = Files.lines(path)) {
            return lines.skip(1).limit(1000).map(this::parseTransaction).toList();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        }
    }

    public List<Transaction> readJavaNIO(String fileName){
        Path path = Path.of(fileName);

        try{
            List<String> lines = Files.readAllLines(path);

            return lines.stream().skip(1).limit(1000).map(this::parseTransaction).toList();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo, via NIO: " + fileName, e);
        }
    }

    public List<Transaction> readJavaIO(String fileName){
        List<Transaction> transactions = new ArrayList<>();

        try(FileInputStream fis = new FileInputStream(fileName);
            Scanner scanner = new Scanner(fis)){

            int lineCount = 0;

            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                lineCount++;

                if(lineCount == 1){
                    continue;
                }

                if (lineCount > 1001) {
                    break;
                }

                Transaction transaction = parseTransaction(line);

                transactions.add(transaction);

            }

        }catch (Exception e){
            throw new RuntimeException("Error ao ler o arquivo, via IO: " + fileName, e);
        }

        return transactions;
    }

    private Transaction parseTransaction(String line) {
        String[] transactionFields = line.split(",");

        Long split = Long.parseLong(transactionFields[0]);
        TransactionType type = TransactionType.valueOf(transactionFields[1]);
        BigDecimal amount = new BigDecimal(transactionFields[2]);
        TransactionCustomer origin = new TransactionCustomer(transactionFields[3], new BigDecimal(transactionFields[4]), new BigDecimal(transactionFields[5]));
        TransactionCustomer destination = new TransactionCustomer(transactionFields[6], new BigDecimal(transactionFields[7]), new BigDecimal(transactionFields[8]));
        boolean isFraud = transactionFields[9].equals("1");
        boolean isFlaggedFraud = transactionFields[10].equals("1");

        return new Transaction(split, type, amount, origin, destination, isFraud, isFlaggedFraud);
    }
}
