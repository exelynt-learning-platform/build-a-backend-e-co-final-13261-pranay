package com.pranay.ecommerce_backend.entity;

public enum Role {
    USER,
    ADMIN,


    /**
     * Default role assigned to new users upon registration.
     */
    DEFAULT_USER(USER.name());

    private final String roleName;

    Role() {
        this.roleName = this.name();
    }

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    }

