package ru.netology.netologycloudstorage.exception;

public class InputDataError extends RuntimeException {
    public InputDataError(String msg) {
        super(msg);
    }
}