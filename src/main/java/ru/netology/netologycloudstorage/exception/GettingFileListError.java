package ru.netology.netologycloudstorage.exception;

public class GettingFileListError extends RuntimeException {
    public GettingFileListError(String msg) {
        super(msg);
    }
}
