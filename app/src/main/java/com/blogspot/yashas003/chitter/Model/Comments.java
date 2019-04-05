package com.blogspot.yashas003.chitter.Model;

public class Comments {

    private String comment;
    private String owner;

    public Comments(String comment, String owner) {
        this.comment = comment;
        this.owner = owner;
    }

    public Comments() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
