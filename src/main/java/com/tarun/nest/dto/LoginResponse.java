package com.tarun.nest.dto;

public class LoginResponse {

    private String token;
    private String message;
    private Long userId;
    private String email;
    private String name;

    public LoginResponse() {
    }

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, String message, Long userId, String email, String name) {
        this.token = token;
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}