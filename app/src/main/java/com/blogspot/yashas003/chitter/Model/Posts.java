package com.blogspot.yashas003.chitter.Model;

import java.util.Date;

public class Posts extends com.blogspot.yashas003.chitter.Utils.PostId {

    private String user_id, image_url, desc, thumb;
    private Date time;

    public Posts() {
    }

    public Posts(String user_id, String image_url, String desc, String thumb, Date time) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.thumb = thumb;
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
