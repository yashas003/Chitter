package com.blogspot.yashas003.chitter.Utils;

import android.support.annotation.NonNull;

public class PostId {

    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.PostId = id;
        return (T) this;
    }
}
