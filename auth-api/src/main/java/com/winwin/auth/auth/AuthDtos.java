package com.winwin.auth.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record Credentials(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 4, max = 72) String password
    ) {
    }

    public record TokenResponse(String token) {
    }
}
