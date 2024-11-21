package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
        String classpath=urlString.substring(0,9);
        if(classpath.equals("classpath")){
            log.info("Папка находится в проекте");
            String resource=urlString.substring(11);
            URL migration_URL = MigrationFileReader.class.getClassLoader().getResource(resource);
            File file1= null;
            try {
                file1 = new File(migration_URL.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if(file1.isDirectory()){
                files = file1.listFiles();
            }
        }
        else{
            log.info("У папки абсолютная директория");
            File file1= null;
            file1 = new File(urlString);
            if(file1.isDirectory()){
                files = file1.listFiles();
            }
        }
        MigrationManager.execute(files);
    }

}

