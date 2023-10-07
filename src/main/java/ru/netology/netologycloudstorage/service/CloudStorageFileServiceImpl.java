package ru.netology.netologycloudstorage.service;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CloudStorageFileServiceImpl implements FileService {

    @Autowired
    CloudStorageFileJpaRepository cloudStorageFileJpaRepository;
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
            link = restClient.getUploadLink(serverPath, true);
        } catch (WrongMethodException | ServerIOException | IOException ex) {
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
            restClient.uploadFile(link, true, tempFile, null);
            result = cloudStorageFileJpaRepository.saveFile(filename, (int) file.getSize(), userId);
            tempFile.delete();
            if (result == 0) {
                deleteFile(userId, filename);
            }
        } catch (ServerException | IOException ex) {
            throw new InputDataError(ex.getMessage());
        }
    }

    public File createTempFile(File tempDir, int userId, MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp_" + userId + "_", ".tmp", tempDir);
        InputStream initialStream = file.getInputStream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        try (OutputStream outStream = new FileOutputStream(tempFile)) {
            outStream.write(buffer);
        }
        return tempFile;
    }

    public byte[] downloadFile(int userId, String filename) {
        if (filename.isBlank()) {
            throw new InputDataError("Error input data");
        }
        String path = rootServerPath + "/" + userId + "/" + filename;
        File saveTo;
        byte[] fileBytes;
        try {
            saveTo = File.createTempFile("temp_" + userId + "_", ".tmp", tempDir);
            restClient.downloadFile(path, saveTo, null);
            fileBytes = new byte[Math.toIntExact(saveTo.length())];
        } catch (ServerException | IOException | ArithmeticException ex) {
            throw new UploadingFileError(ex.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream(saveTo)) {
            fos.write(fileBytes);
        } catch (Exception ex){
            throw new UploadingFileError(ex.getMessage());
        }
        saveTo.delete();
        return fileBytes;
    }

    public void deleteFile(int userId, String filename) {
        String path = rootServerPath + "/" + userId + "/" + filename;
        if (filename.isBlank()) {
            throw new InputDataError("Error input data");
        }
        try {
            restClient.delete(path, true);
            cloudStorageFileJpaRepository.deleteFile(filename, userId);
        } catch (ServerIOException | IOException ex) {
            throw new DeletingFileError(ex.getMessage());
        }
    }

    public void renameFile(String filename, CloudStorageFile newFile, int user_id) {
        if (filename.isBlank() || newFile == null || newFile.getName().isBlank()) {
            throw new InputDataError("Error input data");
        }
        String newFilename = newFile.getName();
        String from = rootServerPath + "/" + user_id + "/" + filename;
        String path = rootServerPath + "/" + user_id + "/" + newFilename;
        Integer result = 0;
        try {
            restClient.move(from, path, false);
            result = cloudStorageFileJpaRepository.renameFile(filename, newFilename, user_id);
            if (result > 0) {
                cloudStorageFileJpaRepository.renameFile(newFilename, filename, user_id);
            }
        } catch (ServerIOException | IOException ex) {
            throw new UploadingFileError(ex.getMessage());
        }
    }

    public void makeFolder(int userId) {
        String path = rootServerPath + "/" + userId;
        try {
            restClient.makeFolder(path);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List<CloudStorageFile> getFilesList(int userId, int limit) {
        if (limit < 0) {
            throw new InputDataError("Error input data");
        }
        Pageable pageRequest = PageRequest.of(0, limit);
        Optional<List<CloudStorageFile>> list = cloudStorageFileJpaRepository.findByOwner(userId, pageRequest);
        List<CloudStorageFile> result = new ArrayList<>();
        if (list.isPresent()) {
            result = list.get();
        } else {
            throw new GettingFileListError("Error getting file list");
        }
        return result;
    }

    public void cleanTempFiles(int userId) {
        Arrays.stream(tempDir.listFiles())
                .iterator()
                .forEachRemaining(n -> {
                    if (n.getName().contains("temp_" + userId)) {
                        n.delete();
                    }
                });
    }
}