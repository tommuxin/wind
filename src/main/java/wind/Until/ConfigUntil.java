package wind.Until;

import com.google.common.collect.Maps;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfigUntil {

    private static Properties properties = new Properties();
    private static Map<String, String> map = Maps.newHashMap();

    private ConfigUntil() {
    }

    public static String getConfig(String key) {
        String value = (String)map.get(key);
        if (null==value) {
            value = properties.getProperty(key);
            map.put(key, value != null ? value : "");
        }
       // System.out.println(key+"......"+value);
        return value;
    }

    static {
        InputStream inputStream = ConfigUntil.class.getClassLoader().getResourceAsStream("add.properties");
        InputStream inputStream1 = ConfigUntil.class.getClassLoader().getResourceAsStream("topic_list.properties");
        try {
            properties.load(inputStream);
            properties.load(inputStream1);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }
}
