package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class  PropertiesUtils {

    private static  Map<String,Object> credantional=new HashMap<>(16);
    private static Logger logger= LoggerFactory.getLogger(PropertiesUtils.class);
    static {
        //сделать проверку на properties или yaml
        try (InputStream inputStream  = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.yaml");) {
            Yaml yaml = new Yaml();
            credantional= yaml.load(inputStream);
            logger.info("Данные успешно обработаны с yaml файла");
        }
        catch (IOException e) {
            logger.error("Данные не взяты с yaml файла "+e.getMessage());
            e.printStackTrace();

        }
    }

    public static Map<String, Object> getCredantional() {
        return credantional;
    }
}
