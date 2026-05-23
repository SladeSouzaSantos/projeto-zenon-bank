package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository{

    @Override
    public void save(Transaction transaction) {
        String sql = """
                INSERT INTO transactions
                (step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, 
                old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud)
                VALUES(?,?,?,?,?,?,?,?,?,?,?);
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ){
            preparedStatement.setInt(1, transaction.step());
            preparedStatement.setString(2, transaction.type().name());
            preparedStatement.setBigDecimal(3, transaction.amount());
            preparedStatement.setString(4, transaction.origin().name());
            preparedStatement.setBigDecimal(5, transaction.origin().oldBalance());
            preparedStatement.setBigDecimal(6, transaction.origin().newBalance());
            preparedStatement.setString(7, transaction.destination().name());
            preparedStatement.setBigDecimal(8, transaction.destination().oldBalance());
            preparedStatement.setBigDecimal(9, transaction.destination().newBalance());
            preparedStatement.setBoolean(10, transaction.isFraud());
            preparedStatement.setBoolean(11, transaction.isFlaggedFraud());

            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar nova transação: " + transaction, e);
        }
    }

    @Override
    public Optional<Transaction> findTransactionByName(String originName) {

        String sql = """
                SELECT id, step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, 
                old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud
                FROM zenon_frauds.transactions
                WHERE name_origin = ?
                ORDER BY step
                LIMIT 1
                """;

        try (Connection connection = ConnectionFactory.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ){
            preparedStatement.setString(1, originName);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    Transaction transaction = mapResultSetToTransaction(resultSet);
                    return Optional.of(transaction);
                }else{
                    IO.println("Transação não encontrada para origin: " + originName);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar transação da origem: " + originName, e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs){
        try {

            int step = rs.getInt("step");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String originName = rs.getString("name_origin");
            BigDecimal oiginOldBalance = rs.getBigDecimal("old_balance_origin");
            BigDecimal oiginNewBalance = rs.getBigDecimal("new_balance_origin");
            TransactionCustomer origin = new TransactionCustomer(originName, oiginOldBalance, oiginNewBalance);

            String recipientName = rs.getString("name_recipient");
            BigDecimal recipientOldBalance = rs.getBigDecimal("old_balance_recipient");
            BigDecimal recipientNewBalance = rs.getBigDecimal("new_balance_recipient");
            TransactionCustomer recipient = new TransactionCustomer(recipientName, recipientOldBalance, recipientNewBalance);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");

            return new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveAll(List<Transaction> transactions){
        String sql = """
                INSERT INTO transactions
                (step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient, 
                old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud)
                VALUES(?,?,?,?,?,?,?,?,?,?,?);
                """;

        try (Connection connection = ConnectionFactory.getConnection()){
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql))
            {
                for (Transaction transaction : transactions){
                    preparedStatement.setInt(1, transaction.step());
                    preparedStatement.setString(2, transaction.type().name());
                    preparedStatement.setBigDecimal(3, transaction.amount());
                    preparedStatement.setString(4, transaction.origin().name());
                    preparedStatement.setBigDecimal(5, transaction.origin().oldBalance());
                    preparedStatement.setBigDecimal(6, transaction.origin().newBalance());
                    preparedStatement.setString(7, transaction.destination().name());
                    preparedStatement.setBigDecimal(8, transaction.destination().oldBalance());
                    preparedStatement.setBigDecimal(9, transaction.destination().newBalance());
                    preparedStatement.setBoolean(10, transaction.isFraud());
                    preparedStatement.setBoolean(11, transaction.isFlaggedFraud());

                    preparedStatement.addBatch();
                }

                IO.println("Executando batch final...");

                preparedStatement.executeBatch();
                connection.commit();

                connection.setAutoCommit(true);
                
            }catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Erro ao executar rollback", ex);
                }

                throw new RuntimeException("Erro ao salvar nova transação...", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro na conexão com o BD...", e);
        }
    }
}
