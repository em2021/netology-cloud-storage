package ru.netology.netologycloudstorage.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.netologycloudstorage.entity.User;
import ru.netology.netologycloudstorage.entity.UserSession;

import java.util.List;
import java.util.Optional;

public interface CloudStorageUserRepositoryCustom {

        @Query(("select u from User u where u.login = :login and u.password = :password"))
        Optional<List<User>> findByLoginAndPassword(@Param("login") String login,
                                                    @Param("password") String password);

        @Query(("select s from UserSession s where s.sessionId ilike :session_id"))
        Optional<UserSession> findBySessionId(@Param("session_id") String sessionId);

        @Modifying
        @Transactional
        @Query(value = "insert into cloud_storage.user_sessions (session_id, user_id) values (:session_id, :user_id)",
                nativeQuery = true)
        void saveSessionId(@Param("session_id") String sessionId,
                           @Param("user_id") Integer userId);

        @Modifying
        @Transactional
        @Query(("delete from UserSession s where s.sessionId ilike :sessionId"))
        void deleteUserSession(@Param("sessionId") String sessionId);
}