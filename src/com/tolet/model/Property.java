package com.tolet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** A property listing that can be rented or bought. */
public class Property implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { APARTMENT, HOUSE, VILLA, STUDIO, DUPLEX, COMMERCIAL;
        public String label() { return name().charAt(0) + name().substring(1).toLowerCase(); }
        @Override public String toString() { return label(); }
    }
    public enum Mode { RENT, SALE;
        public String label() { return this == RENT ? "For Rent" : "For Sale"; }
        @Override public String toString() { return label(); }
    }
    public enum Status { PENDING, AVAILABLE, CLOSED;
        public String label() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }

    public String id, title, description = "", city, area, address = "", ownerId;
    public Type type = Type.APARTMENT;
    public Mode mode = Mode.RENT;
    public Status status = Status.PENDING;
    public double price;
    public int bedrooms, bathrooms, sqft, views;
    public boolean furnished, featured;
    public List<String> amenities = new ArrayList<>();
    public long created = System.currentTimeMillis();

    public String location() { return area + ", " + city; }
}
