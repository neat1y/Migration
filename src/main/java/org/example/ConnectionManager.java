package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class ConnectionManager {
    private static String url;
    private static String name;
    private static String password;
    private static PropertiesUtils propertiesUtils;
    static {
        Map<String,Object> all_parameters= propertiesUtils.getCredantional();
        Map<String,Object> migration_parameters=(Map<String,Object>) all_parameters.get("migration");

        ConnectionManager.url =(String) migration_parameters.get("url");
        ConnectionManager.name =(String) migration_parameters.get("name");
        ConnectionManager.password = (String) migration_parameters.get("password");
    }

    public static Connection getConnection() throws SQLException {
        if(url == null){
            throw new NotAllDataException("You dont have url");
        }
        Connection connection;
        if(!(name != null && password != null)){
            connection= DriverManager.getConnection(url);

        }
        else {
            connection = DriverManager.getConnection(url, name, password);
        }
        return connection;
    }

    public static String getName() {
        return name;
    }
}
