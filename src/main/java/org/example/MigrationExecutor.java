package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MigrationExecutor {

    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);

    public void ddl_query(String sql) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            log.info("all fine");
        } catch (SQLException e) {
            // переделать логирование
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    public void dml_query_for_change(String sql) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement())
        {
            int row =statement.executeUpdate(sql);
            if(row>0){
                log.info("all fine");
            }
            else{
                throw new SQLException("не прошел запрос");
            }
        } catch (SQLException e) {
            // переделать логирование
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    public void dml_query_for_insert(String sql, MigrationSchema migration_schema) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1,migration_schema.getVersion());
            statement.setLong(2,migration_schema.getCRC());
            statement.setString(3,migration_schema.getType());
            statement.setString(4,migration_schema.getInstalled_by());
            statement.setString(5,migration_schema.getFilename());
            statement.executeUpdate();
        } catch (SQLException e) {
            // переделать логирование
            log.error("Ошибка при выполнении SQL: " + e.getMessage()+" " +"SQLState: " + e.getSQLState());
            throw e;
        }
    }
    public List<Long> dml_query_select_CRC(String sql){
        try(Connection connection= ConnectionManager.getConnection();
            Statement statement=connection.createStatement()) {
            List<Long> CRCArray=new ArrayList<>(40);
            ResultSet resultSet= statement.executeQuery(sql);
            while(resultSet.next()){
                CRCArray.add(resultSet.getLong("CRC"));
            }
            return CRCArray;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
