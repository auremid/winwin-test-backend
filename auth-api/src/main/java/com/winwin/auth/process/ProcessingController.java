package com.winwin.auth.process;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class ProcessingController {

    private final ProcessingService processingService;

    public ProcessingController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping
    ProcessDtos.ProcessResponse process(@Valid @RequestBody ProcessDtos.ProcessRequest request,
                                        Authentication authentication) {
        return new ProcessDtos.ProcessResponse(
                processingService.process(authentication.getName(), request.text()));
    }
}
