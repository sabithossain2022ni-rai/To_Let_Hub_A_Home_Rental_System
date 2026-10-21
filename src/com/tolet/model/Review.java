package com.tolet.model;

import java.io.Serializable;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id, propertyId, userId, comment;
    public int rating;
    public long created = System.currentTimeMillis();
}
