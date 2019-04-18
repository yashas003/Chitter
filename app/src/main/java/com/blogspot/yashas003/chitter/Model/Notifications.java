package com.blogspot.yashas003.chitter.Model;

public class Notifications {

    private String user_id, text, post_id;
    private boolean is_post;

    public Notifications(String user_id, String text, String post_id, boolean is_post) {
        this.user_id = user_id;
        this.text = text;
        this.post_id = post_id;
        this.is_post = is_post;
    }

    public Notifications() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public boolean isIs_post() {
        return is_post;
    }

    public void setIs_post(boolean is_post) {
        this.is_post = is_post;
    }
}
