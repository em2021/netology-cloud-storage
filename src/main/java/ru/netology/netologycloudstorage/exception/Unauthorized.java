package ru.netology.netologycloudstorage.exception;

public class Unauthorized extends RuntimeException {
    public Unauthorized(String msg) {
        super(msg);
    }
}
