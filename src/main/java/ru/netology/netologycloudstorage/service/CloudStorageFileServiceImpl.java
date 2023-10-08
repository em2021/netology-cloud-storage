package ru.netology.netologycloudstorage.service;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;
import ru.netology.netologycloudstorage.exception.DeletingFileError;
import ru.netology.netologycloudstorage.exception.GettingFileListError;
import ru.netology.netologycloudstorage.exception.InputDataError;
import ru.netology.netologycloudstorage.exception.UploadingFileError;
import ru.netology.netologycloudstorage.repository.CloudStorageFileJpaRepository;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CloudStorageFileServiceImpl implements FileService {

    @Autowired
    CloudStorageFileJpaRepository cloudStorageFileJpaRepository;
    private final Logger logger = LoggerFactory.getLogger("r.n.ncs.s.CloudStorageFileServiceImpl");
    private final String user = "571cdb7cc0574b0b89fdb30f4eae2b95";
    private final String token = "y0_AgAAAAAx_OgHAAqRLAAAAADtyzvV45v73bZrSpyD6iKRY0kcWgahpws";
    private final Credentials credentials = new Credentials(user, token);
    private RestClient restClient = new RestClient(credentials);
    private final String rootServerPath = "/Приложения/netology-cloud-storage";
    private final File tempDir = new File("src/main/resources/temp");

    public Link getUploadLink(String filename, int userId) {
        String serverPath = rootServerPath + "/" + userId + "/" + filename;
        Link link;
        try {
            logger.info("Sending request to cloud server for a new link for path: {}...", serverPath);
            link = restClient.getUploadLink(serverPath, true);
        } catch (WrongMethodException | ServerIOException | IOException ex) {
            logger.error("Error occurred while receiving a new link for path: {}...", serverPath);
            throw new InputDataError(ex.getMessage());
        }
        return link;
    }

    public void uploadFile(int userId, String filename, MultipartFile file) {
        makeFolder(userId);
        File tempFile;
        Integer result = 0;
        try {
            tempFile = createTempFile(tempDir, userId, file);
            Link link = getUploadLink(filename, userId);
            logger.info("Uploading file: {} to cloud server...", tempFile.getPath());
            restClient.uploadFile(link, true, tempFile, null);
            logger.info("Saving uploaded file details: name: {}; size: {}; userId: {}...",
                    filename,
                    file.getSize(),
                    userId);
            result = cloudStorageFileJpaRepository.saveFile(filename, (int) file.getSize(), userId);
            logger.info("Deleting temporary file at path: {}...", tempFile.getPath());
            boolean res = tempFile.delete();
            if (res) {
                logger.info("Deleting temporary file at path: {}...", tempFile.getPath());
            } else {
                logger.info("Error occurred while deleting temporary file at path: {}...", tempFile.getPath());
            }
            if (result == 0) {
                logger.error("Error occurred while saving details for file: {}; " +
                        "Deleting file from cloud server...", filename);
                deleteFile(userId, filename);
            }
        } catch (ServerException | IOException ex) {
            logger.error("Error occurred while uploading file: {} for user: {}...", filename, userId);
            throw new InputDataError(ex.getMessage());
        }
    }

    public File createTempFile(File tempDir, int userId, MultipartFile file) throws IOException {
        logger.info("Creating temp file at path: {}...", tempDir.getPath());
        File tempFile = File.createTempFile("temp_" + userId + "_", ".tmp", tempDir);
        InputStream initialStream = file.getInputStream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        try (OutputStream outStream = new FileOutputStream(tempFile)) {
            logger.info("Writing to file at path: {}", tempFile.getPath());
            outStream.write(buffer);
        } catch (IOException ex) {
            logger.error("Error occurred while creating " +
                    "temp file at path: {}...", tempFile.getPath());
        }
        logger.info("Temp file at path: {} created successfully...", tempFile.getPath());
        return tempFile;
    }

    public byte[] downloadFile(int userId, String filename) {
        if (filename.isBlank()) {
            logger.error("Unable to download file without name for user: {}...", userId);
            throw new InputDataError("Error input data");
        }
        String path = rootServerPath + "/" + userId + "/" + filename;
        File saveTo;
        byte[] fileBytes;
        try {
            saveTo = File.createTempFile("temp_" + userId + "_", ".tmp", tempDir);
            logger.info("Downloading file to path: {}...", saveTo.getPath());
            restClient.downloadFile(path, saveTo, null);
            fileBytes = new byte[Math.toIntExact(saveTo.length())];
        } catch (ServerException | IOException | ArithmeticException ex) {
            logger.error("Error occurred while downloading file to path: {}...", path);
            throw new UploadingFileError(ex.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream(saveTo)) {
            fos.write(fileBytes);
        } catch (Exception ex) {
            logger.error("Error occurred while retrieving bytes from temporary file: {}...", saveTo.getPath());
            throw new UploadingFileError(ex.getMessage());
        }
        logger.info("Deleting temporary file: {}...", saveTo.getPath());
        saveTo.delete();
        return fileBytes;
    }

    public void deleteFile(int userId, String filename) {
        String path = rootServerPath + "/" + userId + "/" + filename;
        if (filename.isBlank()) {
            logger.error("Unable to delete file without name for user: {}...", userId);
            throw new InputDataError("Error input data");
        }
        try {
            logger.info("Deleting file: {} from cloud server...", filename);
            restClient.delete(path, true);
            logger.info("Deleting row from database for file: {} for user: {}...", filename, userId);
            cloudStorageFileJpaRepository.deleteFile(filename, userId);
        } catch (ServerIOException | IOException ex) {
            logger.error("Error occurred while deleting file: {} for user: {}...", filename, userId);
            throw new DeletingFileError(ex.getMessage());
        }
    }

    public void renameFile(String filename, CloudStorageFile newFile, int user_id) {
        if (filename.isBlank() || newFile == null || newFile.getName().isBlank()) {
            logger.error("Unable to rename file: {} for user: {}...", filename, user_id);
            throw new InputDataError("Error input data");
        }
        String newFilename = newFile.getName();
        String from = rootServerPath + "/" + user_id + "/" + filename;
        String path = rootServerPath + "/" + user_id + "/" + newFilename;
        Integer result = 0;
        try {
            logger.info("Updating file on cloud server for user: {}; old filename: {}; new filename: {}...",
                    user_id,
                    filename,
                    newFilename);
            restClient.move(from, path, false);
            logger.info("Updating file details in database for user: {}; old filename: {}; new filename: {}...",
                    user_id,
                    filename,
                    newFile.getName());
            result = cloudStorageFileJpaRepository.renameFile(filename, newFilename, user_id);
            if (result == 0) {
                logger.error("Error occurred while updating file details in database for " +
                                "user: {}; old filename: {}; new filename: {}...",
                        user_id,
                        filename,
                        newFile.getName());
                logger.warn("Rolling back changes in cloud server for file at path: {} for user: {}...",
                        path,
                        user_id);
                restClient.move(path, from, false);
            }
        } catch (ServerIOException | IOException ex) {
            logger.error("Error occurred while renaming file: {} for user: {}", filename, user_id);
            throw new UploadingFileError(ex.getMessage());
        }
    }

    public void makeFolder(int userId) {
        String path = rootServerPath + "/" + userId;
        try {
            logger.info("Creating folder on cloud server for user: {}, at path: {}", userId, path);
            restClient.makeFolder(path);
        } catch (Exception ex) {
            logger.error("Error occurred while creating folder on cloud server for user: {}, at path: {}", userId, path);
            System.out.println(ex.getMessage());
        }
        logger.info("Folder on cloud server for user: {}, at path: {} created successfully", userId, path);
    }

    public List<CloudStorageFile> getFilesList(int userId, int limit) {
        if (limit < 0) {
            logger.error("Unable to get file list with negative limit for user: {}", userId);
            throw new InputDataError("Error input data");
        }
        Pageable pageRequest = PageRequest.of(0, limit);
        logger.info("Requesting file list for user: {}", userId);
        Optional<List<CloudStorageFile>> list = cloudStorageFileJpaRepository.findByOwner(userId, pageRequest);
        List<CloudStorageFile> result;
        if (list.isPresent()) {
            logger.info("File list for user: {} generated successfully", userId);
            result = list.get();
        } else {
            logger.error("Error occurred while getting file list for user: {}", userId);
            throw new GettingFileListError("Error getting file list");
        }
        return result;
    }

    public void cleanTempFiles(int userId) {
        logger.info("Cleaning temporary files for user: {}", userId);
        AtomicInteger count = new AtomicInteger();
        Arrays.stream(tempDir.listFiles())
                .iterator()
                .forEachRemaining(n -> {
                    if (n.getName().contains("temp_" + userId)) {
                        if (n.delete()) {
                            count.getAndIncrement();
                        }
                    }
                });
        if (count.get() > 0) {
            logger.info("Successfully deleted {} temporary files for user: {}", count.get(), userId);
        }
    }
}