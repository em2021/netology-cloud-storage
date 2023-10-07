package ru.netology.netologycloudstorage.service;

import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileService {

    Link getUploadLink(String filename, int userId) throws WrongMethodException, ServerIOException, IOException;

    void uploadFile(int userId, String filename, MultipartFile file);

    File createTempFile(File tempDir, int userId, MultipartFile file) throws IOException;

    File downloadFile(int userId, String filename);

    void deleteFile(int userId, String filename);

    void renameFile(String filename, CloudStorageFile newFile, int user_id);

    void makeFolder(int userId);

    List<CloudStorageFile> getFilesList(int userId, int limit);

    void cleanTempFiles(int userId);
}