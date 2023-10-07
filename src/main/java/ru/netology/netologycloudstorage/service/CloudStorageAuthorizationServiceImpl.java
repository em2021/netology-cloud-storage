package ru.netology.netologycloudstorage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.netology.netologycloudstorage.entity.User;
import ru.netology.netologycloudstorage.entity.UserSession;
import ru.netology.netologycloudstorage.exception.BadCredentials;
import ru.netology.netologycloudstorage.exception.Unauthorized;
import ru.netology.netologycloudstorage.repository.CloudStorageUserJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CloudStorageAuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    CloudStorageUserJpaRepository cloudStorageUserJpaRepository;

    public String authorizeUser(String login, String password) {
        String sessionId;
        Optional<List<User>> user = cloudStorageUserJpaRepository.findByLoginAndPassword(login, password);
        if (user.isEmpty()) {
            throw new BadCredentials("Bad credentials");
        } else {
            User u = user.get().get(0);
            if (u.getLogin().equals(login) && u.getPassword().equals(password)) {
                sessionId = generateUserSessionId();
                saveSessionId(sessionId, u.getId());
            } else {
                throw new BadCredentials("Bad credentials");
            }
        }
        return sessionId;
    }

    public Integer getUserIdBySession(String authToken) {
        Optional<UserSession> userSession = cloudStorageUserJpaRepository.findBySessionId(trimSessionId(authToken));
        Integer userId = userSession.map(UserSession::getUserId).orElse(null);
        if (userId == null) {
            throw new Unauthorized("Unauthorized error");
        }
        return userId;
    }

    public void logoutUser(String sessionId) {
        cloudStorageUserJpaRepository.deleteUserSession(trimSessionId(sessionId));
    }

    public String generateUserSessionId() {
        return UUID.randomUUID().toString();
    }

    public void saveSessionId(String sessionId, Integer userId) {
        cloudStorageUserJpaRepository.saveSessionId(sessionId, userId);
    }

    public String trimSessionId(String sessionId) {
        return sessionId.replace("Bearer ", "");
    }
}