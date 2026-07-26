package com.tarun.nest.exception;

public class MobileAlreadyExistsException extends RuntimeException {

    public MobileAlreadyExistsException(String message) {
        super(message);
    }

}