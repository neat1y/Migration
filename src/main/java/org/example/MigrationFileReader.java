package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class MigrationFileReader {
    private Path url;
    private static File[] files;
    private Map<String,Object> file_info=new HashMap<>(4);
    private static final Logger log = LoggerFactory.getLogger(MigrationFileReader.class);
    static {
        Map<String,Object> credentional= PropertiesUtils.getCredantional();
        Map<String,Object> migration=(Map<String,Object>)credentional.get("migration");
        Map<String,Object> file=(Map<String,Object>)migration.get("file");
        Map<String,Object> paths= (Map<String,Object>)file.get("path");
        String sort= (String) paths.get("sort");
        String urlString=(String) paths.get("url");
        Boolean rollback=Boolean.FALSE;
        Boolean rollback_yaml=(Boolean) migration.get("type");
        if(rollback_yaml!=null){
            if(rollback_yaml.equals(Boolean.TRUE)){
                rollback=Boolean.TRUE;
            }
        }
        files=getFiles(urlString);

        ArrayList<File> files_final_migrate= new ArrayList<>(files.length);
        ArrayList<File> files_final_rollback=new ArrayList<>(files.length);
//        int i_migrate=0;
//        int i_rollback=0;
        for(File file1 :files) {
            int index = file1.getName().indexOf("__");
            String file_name=file1.getName();
            if(index!=-1){
                if(file_name.charAt(0) == 'U'){
                    files_final_rollback.add(file1);
//                    i_rollback++;
                }
                if(file_name.charAt(0)=='V'){
                    files_final_migrate.add(file1);
//                    i_migrate++;
                }
            }
        }
        if(rollback==Boolean.TRUE){
            log.info("Операции rollback");
            MigrationManager.executeForRollBack(files_final_rollback);
        }
        else {
            log.info("Операции migrate");
            MigrationManager.executeForMigrate(files_final_migrate);
        }
    }
    private static Boolean checkBlockTable(){
        Boolean flag= MigrationManager.checkblockTable();
        return flag;
    }
    private static File[] getFiles(String  urlString){
        String classpath=urlString.substring(0,9);
        if(classpath.equals("classpath")){
            log.info("Папка находится в проекте");
            String resource=urlString.substring(10);
            URL migration_URL = Thread.currentThread().getContextClassLoader().getResource(resource);
            File file1= null;
            try {
                file1 = new File(migration_URL.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if(file1.isDirectory()){
                files = file1.listFiles();
                return files;
            }
            return file1.listFiles();
        }
        else{
            log.info("У папки абсолютная директория");
            File file1= null;
            file1 = new File(urlString);
            if(file1.isDirectory()){
                files = file1.listFiles();
                return files;
            }
            return file1.listFiles();
        }
    }
}

