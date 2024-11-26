package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class ConnectionManager {
    private static String url;
    private static String name;
    private static String password;
    private static PropertiesUtils propertiesUtils;
    private static Logger logger= LoggerFactory.getLogger(ConnectionManager.class);
    // Получаем все данные с propertiesUtil и присваиваем полям url name password данные
    static {
        Map<String,Object> all_parameters= propertiesUtils.getCredantional();
        Map<String,Object> migration_parameters=(Map<String,Object>) all_parameters.get("migration");

        ConnectionManager.url =(String) migration_parameters.get("url");
        ConnectionManager.name =(String) migration_parameters.get("name");
        ConnectionManager.password = (String) migration_parameters.get("password");
    }
    //Получаем connection через стат поля этого класса
    public static Connection getConnection() {
        if(url == null){
            logger.error("You dont have url");
            throw new NotAllDataException("You dont have url");
        }
        Connection connection=null;
        try {


            if (!(name != null && password != null)) {
                connection = DriverManager.getConnection(url);
            } else {
                connection = DriverManager.getConnection(url, name, password);
            }
            logger.info("Connection established");
        }
        catch (SQLException e){
            logger.error("Wrong url or name or password "+ e.getMessage()) ;
            throw new RuntimeException(e.getMessage());
        }
        return connection;
    }

    public static String getName() {
        return name;
    }
}
