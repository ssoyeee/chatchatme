package com.example.chatchatme.model;

import org.w3c.dom.Comment;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {
    public Map<String,Boolean> users = new HashMap<>(); //chat users
    public Map<String,Comment> comments = new HashMap<>(); //chat content
    public long lastTime;

    public static class Comment {
        public String uid;
        public String message;
        public Object timestamp;
     //   public Map<String,Object> readUsers = new HashMap<>();
    }
}
