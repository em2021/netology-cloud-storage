package ru.netology.netologycloudstorage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "user_sessions", schema = "cloud_storage")
public class UserSession {

    @Id
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    @Column(name = "user_id", nullable = false)
    private Integer userId;
}