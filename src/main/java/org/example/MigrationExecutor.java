package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MigrationExecutor {

    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);

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
}
