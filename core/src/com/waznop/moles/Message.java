package com.waznop.moles;

/**
 * Created by Waznop on 2016-12-11.
 */
public class Message {

    private String sender;
    private String content;

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    @Override
    public String toString() {
        return sender.equals("") ? content : sender + ": " + content;
    }
}
