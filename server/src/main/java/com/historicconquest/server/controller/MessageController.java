package com.historicconquest.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public String processMessage(@Payload String message) {
        System.out.println("Message received: " + message);
        return "Received: " + message;
    }
}