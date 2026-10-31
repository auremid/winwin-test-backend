package com.winwin.auth.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProcessDtos {

    private ProcessDtos() {
    }

    public record ProcessRequest(@NotBlank @Size(max = 10_000) String text) {
    }

    public record ProcessResponse(String result) {
    }
}
