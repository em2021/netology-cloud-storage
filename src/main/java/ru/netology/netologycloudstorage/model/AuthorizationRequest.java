package ru.netology.netologycloudstorage.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
@Builder
public class AuthorizationRequest {

    @NotNull
    private String login;
    @NotNull
    private String password;
}