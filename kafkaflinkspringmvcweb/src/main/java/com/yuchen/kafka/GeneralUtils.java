package com.yuchen.kafka;

// 导入所需的日志和I/O类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 创建日期：2023-02-08
 * 工具类，用于加载Kafka配置属性文件
 */
public class GeneralUtils {
    // 使用SLF4J日志接口创建Logger实例，用于记录日志信息
    private static final Logger logger = LoggerFactory.getLogger(GeneralUtils.class);

    /**
     * 加载指定的Kafka属性文件，返回包含配置的Map
     *
     * @param propertyFile 属性文件的名称或路径，指定要加载的Kafka配置文件
     * @return 包含配置项的Map<String, Object>
     */
    public static Map<String, Object> provideKafkaConfig(String propertyFile) {
        // 创建一个空的Map，用于存储属性键值对
        Map<String, Object> configProps = new HashMap<>();
        // 创建Properties对象，用于加载属性文件
        Properties prop = new Properties();
        // 使用try-with-resources语句，确保InputStream在使用后自动关闭
        try (InputStream input = GeneralUtils.class.getClassLoader().getResourceAsStream(propertyFile)) {
            // 加载属性文件到Properties对象
            prop.load(input);
            // 获取所有属性名称的枚举
            Enumeration<?> e = prop.propertyNames();
            // 遍历所有属性名称
            while (e.hasMoreElements()) {
                // 获取属性的键
                String key = (String) e.nextElement();
                // 通过键获取对应的值
                String value = prop.getProperty(key);
                // 将键值对存入Map中
                configProps.put(key, value);
                // 输出键值对（可用于调试）
                System.out.println(key + ":" + value);
            }
        } catch (IOException e) {
            // 当发生IO异常时，记录错误信息并打印堆栈跟踪
            System.out.println("加载kafka属性文件时出现异常");
            e.printStackTrace();
        }
        // 返回包含配置的Map
        return configProps;
    }
}
