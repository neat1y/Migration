package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.StyledEditorKit;
import java.io.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

public class MigrationManager {

    private static final MigrationExecutor migrationExecutor=new MigrationExecutor();
    private static final Logger logger=LoggerFactory.getLogger(MigrationManager.class);
    private static final String username;
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

    static{
        try {
            migrationExecutor.ddl_query(queryForBlock);
            migrationExecutor.ddl_query(queryForAdditionalTable);
        } catch (SQLException e) {
            logger.error("SQL exception "+ e.getErrorCode());
            throw new RuntimeException(e);
        }
        username=ConnectionManager.getName();
    }
    public static Long get_CRC(File file){
        CRC32 crc32 = new CRC32();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }

            long checksum = crc32.getValue();
            return checksum;
        } catch (IOException e) {
           logger.error("Ошибка при чтении файла: " + e.getMessage());
           return 0L;
        }
    }


    public  static void execute(File [] files){
        List<Long> array_CRC= migrationExecutor.dml_query_select_CRC(SelectCRC);
        for(File file:files){
            StringBuilder query=new StringBuilder();
            try(BufferedReader br=new BufferedReader(new FileReader(file))){
                String line;
                Long CRC_File= get_CRC(file);
                Boolean flag=Boolean.FALSE;
                for(int i=0;i<array_CRC.size();i++){
                    if(CRC_File.equals(array_CRC.get(i))){
                        flag=Boolean.TRUE;
                        break;
                    }
                }
                if(flag==Boolean.TRUE){
                    continue;
                }
                while((line = br.readLine()) != null) {
                    query.append(line);
                    if(query.toString().contains(";")){
                        if(query.toString().contains("select")
                                || query.toString().contains("insert")
                                || query.toString().contains("update")
                                || query.toString().contains("DELETE")
                        ) {
                            migrationExecutor.dml_query_for_change(query.toString());
                        }
                        else{
                            migrationExecutor.ddl_query(query.toString());
                        }
                        query = new StringBuilder();

                    }
                }

                String type="-";
                String version="-";
                if(file.getName().indexOf(".")>  -1){
                    type = file.getName().substring( file.getName().indexOf(".")+ 1);
                }
                if(file.getName().indexOf("__")>-1){
                    version =file.getName().substring(0,file.getName().indexOf("__"));
                }

                MigrationSchema migrationSchema=new MigrationSchema();
                migrationSchema.setFilename(file.getName());
                migrationSchema.setCRC(CRC_File);
                migrationSchema.setType(type);
                migrationSchema.setInstalled_by(username);
                migrationSchema.setVersion(version);
                migrationExecutor.dml_query_for_insert(insertMigrationSchema,migrationSchema);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
