package ru.netology.netologycloudstorage.exception;

public class DeletingFileError extends RuntimeException {
    public DeletingFileError(String msg) {
        super(msg);
    }
}