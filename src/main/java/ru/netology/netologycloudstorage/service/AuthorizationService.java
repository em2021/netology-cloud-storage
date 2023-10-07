package ru.netology.netologycloudstorage.service;

import java.sql.SQLException;

public interface AuthorizationService {

    String authorizeUser(String login, String password) throws SQLException;

    Integer getUserIdBySession(String authToken);

    void logoutUser(String sessionId) throws SQLException;

    String generateUserSessionId();

    void saveSessionId(String sessionId, Integer userId) throws SQLException;

    String trimSessionId(String sessionId);
}