package com.yuchen.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Created by yuchen on 2023-02-08.
 * 在创建WebSocket处理程序类之后，通过实现WebSocketConfigurer接口来创建@Configuration类。
 * 使用@EnableWebSocket注释注释WebSocket配置类来处理WebSocket请求。
 * 在WebSocketConfigurer接口的被重写的方法registerWebSocketHandlers()中注册WebSocket处理程序类。
 * ServletServerContainerFactoryBean可以添加对WebSocket的一些配置
 *
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private MyWebSocketServer socketHandler;

    @Autowired
    public WebSocketConfig(MyWebSocketServer socketHandler) {
        this.socketHandler = socketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("==========================注册socket");
        //注册处理拦截器,拦截url为socketServer的请求
        registry.addHandler(socketHandler, "/dashboard")
//                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*");        // 允许跨域访问

        //注册SockJs的处理拦截器,拦截url为/sockjs/socketServer的请求
        registry.addHandler(socketHandler, "/socketjs_dashboard")
//                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*")         // 允许跨域访问
                .withSockJS();
    }

//    @Bean
//    public ServletServerContainerFactoryBean createWebSocketContainer() {
//        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//        container.setMaxTextMessageBufferSize(8192*4);
//        container.setMaxBinaryMessageBufferSize(8192*4);
//        return container;
//    }

}
