package com.tolet.model;

import java.io.Serializable;

/** A registered person: customer, property owner or admin. */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role { CUSTOMER, OWNER, ADMIN;
        public String label() { return name().charAt(0) + name().substring(1).toLowerCase(); }
    }

    public String id, name, email, phone, passHash;
    public Role role;
    public boolean blocked;
    public long joined = System.currentTimeMillis();

    public User(String id, String name, String email, String phone, String passHash, Role role) {
        this.id = id; this.name = name; this.email = email;
        this.phone = phone; this.passHash = passHash; this.role = role;
    }

    @Override public String toString() { return name; }
}
