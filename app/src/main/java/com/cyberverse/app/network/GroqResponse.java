package com.cyberverse.app.network;

import java.util.List;

public class GroqResponse {
    public String id;
    public List<Choice> choices;
    public Error error;

    public static class Choice {
        public int index;
        public Message message;
        public String finish_reason;
    }

    public static class Message {
        public String role;
        public String content;
    }

    public static class Error {
        public String message;
        public String type;
        public String code;
    }

    public String getFirstChoiceText() {
        if (choices != null && !choices.isEmpty()) {
            Choice choice = choices.get(0);
            if (choice != null && choice.message != null && choice.message.content != null) {
                return choice.message.content;
            }
        }
        return null;
    }
}
