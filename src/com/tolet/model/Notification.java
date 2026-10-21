package com.tolet.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id, userId, text;
    public boolean read;
    public long time = System.currentTimeMillis();
}
