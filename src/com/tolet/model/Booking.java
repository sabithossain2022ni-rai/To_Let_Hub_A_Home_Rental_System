package com.tolet.model;

import java.io.Serializable;

/** A visit appointment or a rent / buy request made by a customer. */
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Kind { VISIT, RENT_REQUEST, BUY_REQUEST;
        public String label() {
            switch (this) { case VISIT: return "Site Visit"; case RENT_REQUEST: return "Rent Request"; default: return "Buy Request"; }
        }
    }
    public enum Status { PENDING, APPROVED, REJECTED, CANCELLED;
        public String label() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }

    public String id, propertyId, customerId, date, time = "", note = "";
    public Kind kind;
    public Status status = Status.PENDING;
    public double offer;
    public long created = System.currentTimeMillis();
}
