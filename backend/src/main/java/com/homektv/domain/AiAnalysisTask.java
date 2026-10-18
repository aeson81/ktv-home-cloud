package com.homektv.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

/**
 * AI 分析任务实体，用于跟踪歌曲的 AI 分析请求、重试次数、分析结果及错误信息。
 *
 * Entity representing an AI analysis task that tracks analysis requests for songs,
 * including retry attempts, analysis results, and error information.
 */
@Entity
@Table(name = "ai_analysis_tasks")
public class AiAnalysisTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "song_id")
    private Long songId;

    @Column(name = "target_type", nullable = false)
    private String targetType = "SONG";

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "model_role", nullable = false)
    private String modelRole = "BULK";

    @Column(name = "field_confidence", columnDefinition = "jsonb", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String fieldConfidence = "{}";

    @Column(columnDefinition = "jsonb", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String evidence = "{}";

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(nullable = false)
    private String model;

    @Column(name = "result_json", columnDefinition = "text")
    private String resultJson;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Transient
    private String targetTitle;

    @Transient
    private String targetArtist;

    // ---- getters / setters ----
    public Long getId() { return id; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getModelRole() { return modelRole; }
    public void setModelRole(String modelRole) { this.modelRole = modelRole; }
    public String getFieldConfidence() { return fieldConfidence; }
    public void setFieldConfidence(String fieldConfidence) { this.fieldConfidence = fieldConfidence; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public String getTargetTitle() { return targetTitle; }
    public void setTargetTitle(String targetTitle) { this.targetTitle = targetTitle; }
    public String getTargetArtist() { return targetArtist; }
    public void setTargetArtist(String targetArtist) { this.targetArtist = targetArtist; }
}
