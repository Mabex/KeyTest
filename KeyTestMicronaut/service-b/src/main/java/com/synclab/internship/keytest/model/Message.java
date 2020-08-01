package com.synclab.internship.keytest.model;

import java.io.Serializable;

public class Message implements Serializable {

    private String from;
    private String body;
    private String signature;

    public Message() {
        this.from = null;
        this.body = null;
        this.signature = null;
    }

    public Message(String from, String body, String signature) {
        this.from = from;
        this.body = body;
        this.signature = signature;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "Message{" +
                "from='" + from + '\'' +
                ", body='" + body + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
