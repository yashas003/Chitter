package com.blogspot.yashas003.chitter.Model;

public class Notifications {

    private String user_id, text, post_id, image_url, time;
    private boolean is_post;

    public Notifications(String user_id, String text, String post_id, String image_url, String time, boolean is_post) {
        this.user_id = user_id;
        this.text = text;
        this.post_id = post_id;
        this.image_url = image_url;
        this.time = time;
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isIs_post() {
        return is_post;
    }

    public void setIs_post(boolean is_post) {
        this.is_post = is_post;
    }
}
