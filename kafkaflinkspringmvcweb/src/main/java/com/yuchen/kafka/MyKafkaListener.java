package com.yuchen.kafka;

import com.yuchen.socket.MyWebSocketServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by yuchen on 2023-02-08.
 *
 * Kafka监听器，用于监听Kafka中的消息，并将消息推送到WebSocket客户端
 */

@Service
public class MyKafkaListener implements MessageListener<String,String>{

    private MyWebSocketServer socketHandler;

    @Autowired
    public MyKafkaListener(MyWebSocketServer socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        // 提取kafka消息中的value部分
        String msg = record.value();
        System.out.println("---收到的kafka消息---");
        System.out.println(msg);
        try {
            // 向客户端socket推送消息
            socketHandler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
