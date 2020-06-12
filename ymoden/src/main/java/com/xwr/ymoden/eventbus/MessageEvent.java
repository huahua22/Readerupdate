package com.xwr.ymoden.eventbus;

/**
 * 消息事件
 */

public class MessageEvent {
    public MessageEvent() {
    }

    public MessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;
}
