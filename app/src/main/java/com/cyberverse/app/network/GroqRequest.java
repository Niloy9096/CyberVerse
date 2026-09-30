package com.cyberverse.app.network;

import java.util.List;

public class GroqRequest {
    public String model;
    public List<Message> messages;
    public double temperature = 0.7;
    public int max_tokens = 1024;

    public GroqRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
