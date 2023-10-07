package ru.netology.netologycloudstorage.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.netologycloudstorage.entity.CloudStorageFile;
import ru.netology.netologycloudstorage.model.AuthorizationRequest;
import ru.netology.netologycloudstorage.model.AuthorizationResponse;
import ru.netology.netologycloudstorage.service.AuthorizationService;
import ru.netology.netologycloudstorage.service.FileService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

@RestController
public class CloudStorageController {

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    FileService fileService;

    @PostMapping("/login")
    public ResponseEntity<AuthorizationResponse> login(@RequestBody AuthorizationRequest authRequest)
            throws SQLException {
        return ResponseEntity.ok(new AuthorizationResponse(authorizationService
                .authorizeUser(authRequest.getLogin(), authRequest.getPassword())));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("auth-token") @NotNull String authToken) throws SQLException {
        authorizationService.logoutUser(authToken);
        fileService.cleanTempFiles(authorizationService.getUserIdBySession(authToken));
        return ResponseEntity.ok("Success logout");
    }

    @PostMapping("/file")
    public ResponseEntity<String> uploadFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String filename,
                                             @RequestBody @NotNull MultipartFile file) {
        fileService.uploadFile(authorizationService.getUserIdBySession(authToken), filename, file);
        return ResponseEntity.ok("Success upload");
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestHeader("auth-token") @NotNull String authToken,
                                             @RequestParam("filename") @NotNull String filename) {
        fileService.deleteFile(authorizationService.getUserIdBySession(authToken), filename);
        return ResponseEntity.ok("CloudStorageFile deleted successfully");
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestHeader("auth-token") @NotNull String authToken,
                                          @RequestParam("filename") @NotNull String filename) {
        File file = fileService.downloadFile(authorizationService.getUserIdBySession(authToken), filename);
        return ResponseEntity.ok(file);
    }

    @PutMapping("/file")
    public ResponseEntity<String> changeFilename(@RequestHeader("auth-token") @NotNull String authToken,
                                                 @RequestParam("filename") @NotNull String filename,
                                                 @RequestBody @NotNull CloudStorageFile newFile) {
        fileService.renameFile(filename, newFile, authorizationService.getUserIdBySession(authToken));
        return ResponseEntity.ok("Filename changed successfully.");
    }

    @GetMapping("/list")
    public ResponseEntity<List<CloudStorageFile>> list(@RequestHeader("auth-token") @NotNull String authToken,
                                                       @RequestParam("limit") @NotNull Integer limit) {
        return ResponseEntity.ok(fileService.getFilesList(authorizationService.getUserIdBySession(authToken), limit));
    }
}