package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Класс нужен для выполненение запрос в бд
public class MigrationExecutor {
    // Sql запрос для вставки значение в таблицу block если она пуста
    private  static String insertTableBlock="insert into block (flag) values(false);";
    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);
    // Sql запрос для обновлении строк  в таблице block для разблокировки dml запросов(В моем случае блокировка идет только на dml запросы)
    private static  final String updateTableBlock="update block set flag=?;";
    // Выполнение ddl запросов через обычный Statement
    public void ddl_query(String sql,Connection connection) throws SQLException {
        try (
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            log.info("ddl query passed successfully");
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    // Выполнение dml на изменение запросов через обычный Statement
    public void dml_query_for_change(String sql,Connection connection) throws SQLException {
        try (
             Statement statement = connection.createStatement();)
        {
            int row =statement.executeUpdate(sql);
            if(row>0){
                log.info("dml query passed successfully");
            }
            else{
                log.error("Dml query didn't pass");
                throw new SQLException("не прошел запрос");
            }
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    // Выполнение dml на встсавку в таблицу migration_schema через  PreparedStatement чтобы добавлять информацию о строке
    public void dml_query_for_insert(String sql, MigrationSchema migration_schema,Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);)
        {

            statement.setString(1,migration_schema.getVersion());
            statement.setLong(2,migration_schema.getCRC());
            statement.setString(3,migration_schema.getType());
            statement.setString(4,migration_schema.getInstalled_by());
            statement.setString(5,migration_schema.getFilename());
            statement.executeUpdate();
            log.info("Query  for insert migration_schema Successfully");
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    // Выполнение dml запросы на select чтобы взять crc из таблицы migration_schema,
    // для проверки, был ли этот файл выполнен раньше или нет
    public List<Long> dml_query_select_CRC(String sql,Connection connection){
        try(Statement statement=connection.createStatement()) {
            List<Long> CRCArray=new ArrayList<>(40);
            ResultSet resultSet= statement.executeQuery(sql);
            while(resultSet.next()){
                CRCArray.add(resultSet.getLong("CRC"));
            }
            log.info("Query select crc Successfully");
            return CRCArray;
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw new RuntimeException(e);
        }
    }
    // dml запрос на проверку блокировки если Resultset дает true то таблица заюлокирована и нельзя ничего делать
    public Boolean dmlQueryForBlock(String sql,Connection connection){
        try(Statement statement=connection.createStatement()) {
            Boolean flag=Boolean.FALSE;
            ResultSet resultSet= statement.executeQuery(sql);
            if (resultSet.next()) {
                flag = resultSet.getBoolean("flag");
            } else {
                statement.executeUpdate(insertTableBlock);
            }
            log.info("Query select crc Successfully");
            return flag;
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw new RuntimeException(e);
        }
    }
    // Изменение значение flag в таблицу block чтобы захватить ресурс
    public Boolean changeValueBlock(Boolean flag,Connection connection){
        try(PreparedStatement statement=connection.prepareStatement(updateTableBlock)) {
            statement.setBoolean(1,flag);
            int rowsAffected = statement.executeUpdate();
            log.info("Query select crc Successfully");
            return flag;
        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw new RuntimeException(e);
        }
    }
}
