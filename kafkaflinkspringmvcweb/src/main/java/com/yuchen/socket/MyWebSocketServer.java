package com.yuchen.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuchen on 2023-02-08.
 * WebSocket Server
 * 要处理WebSocket消息，首先需要创建一个WebSocket处理程序类(即WebSocket服务器)
 * 这通过继承 TextWebSocketHandler 或 BinaryWebSocketHandler 类来创建。
 */
@Service
public class MyWebSocketServer extends TextWebSocketHandler {

//    private static final Logger logger = LoggerFactory.getLogger(MyWebSocketServer.class);

    // 用ArrayList 来维护所有成功握手长连接
    private static List<WebSocketSession> sessions = new ArrayList<>();

    /**
     * 三次握手成功，进入这个方法处理，将session 加入list 中
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
//        logger.info("用户{}连接成功.... ",session);
        System.out.println("已连接成功.... ");
    }

    /*
      关闭连接进入这个方法处理，将session从 list中删除
     */
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        sessions.remove(session);
//        logger.info("{} 连接已经关闭，现从list中删除 ,状态信息{}", session, status);
//        System.out.println(session + "连接已经关闭，现从list中删除 ,状态信息" + status);
//    }

//    /**
//     * 处理客户发送的信息，将客户发送的信息转给其他用户
//     */
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String clientMessage = message.getPayload();
//        logger.info("reveice client msg: {}", clientMessage);
//        System.out.println("reveice client msg: " + clientMessage);
//    }

    /**
     * 自定义推送消息的方法
     */
    public void sendMessage(String msg) throws IOException {
        for(WebSocketSession session : sessions) {
            session.sendMessage(new TextMessage(msg));
        }
    }

}
