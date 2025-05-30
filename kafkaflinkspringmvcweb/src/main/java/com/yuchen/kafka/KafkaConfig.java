package com.yuchen.kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import java.util.Map;

/**
 * Created by yuchen on 2023-02-08.
 * 配置Kafka监听器容器和监听器
 *
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private MyKafkaListener myKafkaListener;

    // 构造器依赖注入
    @Autowired
    public KafkaConfig(MyKafkaListener myKafkaListener) {
        this.myKafkaListener = myKafkaListener;
        System.out.println("MyKafkaListener Bean 加载成功");
    }

    // 加载Kafka配置
    private Map<String, Object> kafkaConfig(){
        Map<String, Object> props = GeneralUtils.provideKafkaConfig("kafka.properties");
        return props;
    }

    // 创建消费者对象
    private ConsumerFactory<String, String> consumerProps() {
        Map<String, Object> props = kafkaConfig();
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // 配置消息监听容器
    @Bean
    public ConcurrentMessageListenerContainer<String, String> messageListenerContainer() {
        ContainerProperties containerProps = new ContainerProperties("fastcars");
        containerProps.setMessageListener(this.myKafkaListener);        // 设置监听器

        ConcurrentMessageListenerContainer<String, String> listenerContainer =
                new ConcurrentMessageListenerContainer<>(consumerProps(), containerProps);
        listenerContainer.setConcurrency(1);
        listenerContainer.getContainerProperties().setPollTimeout(5000);

        return listenerContainer;
    }
}
