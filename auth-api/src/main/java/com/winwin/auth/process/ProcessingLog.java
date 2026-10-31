package com.winwin.auth.process;

import com.winwin.auth.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_log")
public class ProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "input_text", nullable = false, columnDefinition = "text")
    private String inputText;

    @Column(name = "output_text", nullable = false, columnDefinition = "text")
    private String outputText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProcessingLog() {
    }

    public ProcessingLog(UserAccount user, String inputText, String outputText) {
        this.user = user;
        this.inputText = inputText;
        this.outputText = outputText;
    }

    @PrePersist
    void setCreationTime() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getInputText() {
        return inputText;
    }

    public String getOutputText() {
        return outputText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
