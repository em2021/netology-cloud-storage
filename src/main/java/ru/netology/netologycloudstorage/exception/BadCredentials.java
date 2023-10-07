package ru.netology.netologycloudstorage.exception;

public class BadCredentials extends RuntimeException {

    public BadCredentials(String msg) {
        super(msg);
    }
}
