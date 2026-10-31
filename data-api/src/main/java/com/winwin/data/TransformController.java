package com.winwin.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

@RestController
@RequestMapping("/api/transform")
public class TransformController {

    private final byte[] internalToken;

    public TransformController(@Value("${app.internal-token}") String internalToken) {
        if (internalToken.isBlank()) {
            throw new IllegalStateException("INTERNAL_TOKEN must not be blank");
        }
        this.internalToken = internalToken.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    ResponseEntity<?> transform(
            @RequestHeader(name = "X-Internal-Token", required = false) String providedToken,
            @Valid @RequestBody TransformRequest request) {
        if (!validToken(providedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(new TransformResponse(request.text().toUpperCase(Locale.ROOT)));
    }

    private boolean validToken(String providedToken) {
        return providedToken != null && MessageDigest.isEqual(
                internalToken, providedToken.getBytes(StandardCharsets.UTF_8));
    }

    record TransformRequest(@NotBlank @Size(max = 10_000) String text) {
    }

    record TransformResponse(String result) {
    }
}
