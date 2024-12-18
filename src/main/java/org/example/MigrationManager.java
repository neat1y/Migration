package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class MigrationManager {

    private static final MigrationExecutor migrationExecutor=new MigrationExecutor();
    private static final Logger logger=LoggerFactory.getLogger(MigrationManager.class);
    private static final String username;

   private static final String deleteRowMigration= """
           delete from migration_schema where version=?
           """;
    private static final String selectForblock= """
           select from block
           """;

    private static final String queryForAdditionalTable= "CREATE TABLE IF NOT EXISTS migration_schema (\n" +
            "    installed_rank INT PRIMARY KEY generated  by default as identity ,\n" +
            "    version VARCHAR(50) NOT NULL,\n" +
            "    CRC BIGINT,\n" +
            "    type VARCHAR(100),\n"+
            "    installed_by VARCHAR(100) NOT NULL,\n" +
            "    filename VARCHAR(200) NOT NULL" +
            ");\n";

    private static final String queryForBlock= "CREATE TABLE  IF NOT EXISTS block(" +
            "flag boolean  default false"+
            ");\n";

    private static final String insertMigrationSchema="INSERT INTO migration_schema " +
            "(version,crc,type,installed_by,filename) values(?,?,?,?,?);";
    private static final String SelectCRC="SELECT crc FROM migration_schema;";

    private static final String checkBlockTable= """
            Select * from block;
            """;
    //Сразу выполняются запросы на добавление 2 таблиц это block и migration_schema если они уже есть то запросы не будут выполнены
    static{
        try {
            Connection connection=ConnectionManager.getConnection();
            migrationExecutor.ddl_query(queryForBlock,connection);
            migrationExecutor.ddl_query(queryForAdditionalTable,connection);
        } catch (SQLException e) {
            logger.error("SQL exception "+ e.getErrorCode());
            throw new RuntimeException(e);
        }
        username=ConnectionManager.getName();
    }
    // Отдельный  метод для посчета CRC в одном файле
    public static Long get_CRC(File file){
        CRC32 crc32 = new CRC32();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
            long checksum = crc32.getValue();
            logger.info("Посчитана CRC файла " + checksum);
            return checksum;
        } catch (IOException e) {
           logger.error("Ошибка при чтении файла: " + e.getMessage());
           return 0L;
        }
    }

    // след 2 метода нужны для разделенеие на migrate or rollback это нужна для возможной изменении логики в rollback

    public  static void executeForMigrate(ArrayList<File> files){
        Connection connection = null;
        try {
            connection=ConnectionManager.getConnection();
            connection.setAutoCommit(false);
            List<Long> array_CRC = migrationExecutor.dml_query_select_CRC(SelectCRC,connection);
            executeFiles(files,array_CRC,connection);
            connection.commit();
            logger.info("All operations were completed");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("error with rollback " +ex.getMessage());
            }
            throw new RuntimeException(e);
        }

    }
    //
    public static void executeForRollBack(ArrayList<File> files){
        Connection connection = null;
        try {
            connection=ConnectionManager.getConnection();
            connection.setAutoCommit(false);
            List<Long> array_CRC = migrationExecutor.dml_query_select_CRC(SelectCRC,connection);
            executeFiles(files,array_CRC,connection);
            connection.commit();
            logger.info("All operations were completed");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.error("error with rollback " +ex.getMessage());
            }
            throw new RuntimeException(e);
        }

    }

    // Выполняет все файлы которые подаются на вход и проверяет каждый CRC  с каждым файлом
    private static void executeFiles(ArrayList<File> files,List<Long> array_CRC,Connection connection) throws SQLException {
        for (File file : files) {
            StringBuilder query = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                Long CRC_File = get_CRC(file);
                Boolean flag = Boolean.FALSE;
                for (int i = 0; i < array_CRC.size(); i++) {
                    if (CRC_File.equals(array_CRC.get(i))) {
                        flag = Boolean.TRUE;
                        break;
                    }
                }
                if (flag == Boolean.TRUE) {
                    // Если повтор был то не считываем этот файл а просто переходим к след
                    continue;
                }
                while ((line = br.readLine()) != null) {
                    query.append(line);
                    // Строка конкатенируется с линией до знака ';' когда он видит ';' то он проверяет какой это запрос
                    if (query.toString().contains(";")) {
                        // Если это dml запрос то идет одна логика если ddl другая
                        if (query.toString().contains("select")
                                || query.toString().contains("insert")
                                || query.toString().contains("update")
                                || query.toString().contains("DELETE")
                        ) {
                            //Если это dml зпрос будет работать блокировка, сначала проверяет открыт ли ресурс, если да то
                            // захватывает его, если нет ждет 100 млс и делает еще раз запрос
                            Boolean block= migrationExecutor.dmlQueryForBlock(selectForblock,connection);
                             if(block!=Boolean.FALSE) {
                                    while(block!=Boolean.FALSE){
                                        Thread.sleep(100);
                                        block=migrationExecutor.dmlQueryForBlock(selectForblock,connection);
                                    }
                             }
                             migrationExecutor.changeValueBlock(Boolean.TRUE,connection);
                             migrationExecutor.dml_query_for_change(query.toString(), connection);
                             migrationExecutor.changeValueBlock(Boolean.FALSE,connection);
                        } else {
                            // на ddl я не делал блокировку
                            migrationExecutor.ddl_query(query.toString(),connection);
                        }
                        query = new StringBuilder();

                    }
                }
                // Собираем информацию для migration_schema
                String type = "-";
                String version = "-";
                if (file.getName().indexOf(".") > -1) {
                    type = file.getName().substring(file.getName().indexOf(".") + 1);
                }
                if (file.getName().indexOf("__") > -1) {
                    version = file.getName().substring(0, file.getName().indexOf("__"));
                }

                MigrationSchema migrationSchema = new MigrationSchema();
                migrationSchema.setFilename(file.getName());
                migrationSchema.setCRC(CRC_File);
                migrationSchema.setType(type);
                migrationSchema.setInstalled_by(username);
                migrationSchema.setVersion(version);
                //Делаем запром в migration_schema
                migrationExecutor.dml_query_for_insert(insertMigrationSchema, migrationSchema,connection);

            } catch (FileNotFoundException | SQLException | InterruptedException e) {
                try {
                    // Если была какая то ошибка Освобождаем ресурс
                    migrationExecutor.changeValueBlock(Boolean.FALSE,connection);
                    throw new SQLException(e);
                } catch (SQLException ex) {

                    // Если была какая то ошибка Освобождаем ресурс
                    migrationExecutor.changeValueBlock(Boolean.FALSE,connection);
                    throw new RuntimeException(ex);
                }
            } catch (IOException e) {
                // Если была какая то ошибка Освобождаем ресурс
                migrationExecutor.changeValueBlock(Boolean.FALSE,connection);
                throw new SQLException(e);
            }
        }
    }

    private static Boolean checkblockTable() {
        Connection connection=null;
        connection=ConnectionManager.getConnection();
        return migrationExecutor.dmlQueryForBlock(selectForblock,connection);

    }
}
