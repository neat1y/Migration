package org.example;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class  PropertiesUtils {

    private static  Properties properties = new Properties();
    private static  Map<String,Object> credantional=new HashMap<>(16);
    private static Boolean flag=Boolean.FALSE;
    static {
        //сделать проверку на properties или yaml
        try (InputStream inputStream  = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.yaml");) {
            Yaml yaml = new Yaml();
            credantional= yaml.load(inputStream);
            flag=Boolean.TRUE;
        }
        catch (IOException e) {
            flag=Boolean.FALSE;
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getCredantional() {
        return credantional;
    }
}
