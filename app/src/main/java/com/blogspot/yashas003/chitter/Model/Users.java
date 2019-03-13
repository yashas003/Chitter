package com.blogspot.yashas003.chitter.Model;

public class Users {

    private String user_id, user_image, user_name, unique_name;

    public Users(String user_id, String user_image, String user_name, String unique_name) {
        this.user_id = user_id;
        this.user_image = user_image;
        this.user_name = user_name;
        this.unique_name = unique_name;
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

    public String getUnique_name() {
        return unique_name;
    }

    public void setUnique_name(String unique_name) {
        this.unique_name = unique_name;
    }
}
