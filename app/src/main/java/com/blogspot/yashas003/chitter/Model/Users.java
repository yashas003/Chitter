package com.blogspot.yashas003.chitter.Model;

public class Users {

    private String user_id, user_image, user_name, user_bio;

    public Users(String user_id, String user_image, String user_name, String user_bio) {
        this.user_id = user_id;
        this.user_image = user_image;
        this.user_name = user_name;
        this.user_bio = user_bio;
    }

    public Users() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio = user_bio;
    }
}
