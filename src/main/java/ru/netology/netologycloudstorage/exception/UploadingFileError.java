package ru.netology.netologycloudstorage.exception;

public class UploadingFileError extends RuntimeException {
    public UploadingFileError(String msg) {
        super(msg);
    }
}
