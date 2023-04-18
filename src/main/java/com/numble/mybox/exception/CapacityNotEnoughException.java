package com.numble.mybox.exception;

public class CapacityNotEnoughException extends RuntimeException {
    public CapacityNotEnoughException(String message) {
        super(message);
    }
}
