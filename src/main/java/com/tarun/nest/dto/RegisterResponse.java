package com.tarun.nest.dto;

public class RegisterResponse {

    private Long id;

    private String name;

    private String email;

    private String mobileNumber;

    private String role;

    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id,
                            String name,
                            String email,
                            String mobileNumber,
                            String role,
                            String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }
}