package ru.netology.netologycloudstorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger("r.n.ncs.s.CloudStorageAuthorizationService");

    public String authorizeUser(String login, String password) {
        String sessionId;
        logger.info("Searching for user with login: {}...", login);
        Optional<List<User>> user = cloudStorageUserJpaRepository.findByLoginAndPassword(login, password);
        if (user.isEmpty()) {
            logger.error("User with login: {} doesn't exist", login);
            throw new BadCredentials("Bad credentials");
        } else {
            User u = user.get().get(0);
            if (u.getLogin().equals(login) && u.getPassword().equals(password)) {
                logger.info("User with login: {} found...", login);
                logger.info("Generating session id for user {}...", login);
                sessionId = generateUserSessionId();
                logger.info("Saving session id for user {}...", login);
                saveSessionId(sessionId, u.getId());
            } else {
                logger.error("Invalid login: {}; password: {}", login, password);
                throw new BadCredentials("Bad credentials");
            }
        }
        return sessionId;
    }

    public Integer getUserIdBySession(String authToken) {
        logger.info("Searching for user session: {}...", authToken);
        Optional<UserSession> userSession = cloudStorageUserJpaRepository.findBySessionId(trimSessionId(authToken));
        Integer userId = userSession.map(UserSession::getUserId).orElse(null);
        if (userId == null) {
            logger.error("Session: {} does not exist...", authToken);
            throw new Unauthorized("Unauthorized error");
        }
        logger.info("User session: {} of user: {} found successfully...", authToken, userId);
        return userId;
    }

    public void logoutUser(String sessionId) {
        logger.info("Searching for user session: {} to delete...", sessionId);
        Integer result = cloudStorageUserJpaRepository.deleteUserSession(trimSessionId(sessionId));
        if (result == 0) {
            logger.error("User session: {} not found...", sessionId);
        } else {
            logger.info("User session: {} deleted successfully...", sessionId);
        }
    }

    public String generateUserSessionId() {
        return UUID.randomUUID().toString();
    }

    public void saveSessionId(String sessionId, Integer userId) {
        Integer result = cloudStorageUserJpaRepository.saveSessionId(sessionId, userId);
        if (result == 0) {
            logger.warn("User session: {} not saved...", sessionId);
        } else {
            logger.info("User session: {} saved successfully...", sessionId);
        }
    }

    public String trimSessionId(String sessionId) {
        logger.info("Trimming session: {}...", sessionId);
        return sessionId.replace("Bearer ", "");
    }
}