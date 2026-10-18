package com.homektv.repo;

import com.homektv.domain.AiAnalysisTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * AI分析任务数据访问层，对应 ai_analysis_task 表。
 *
 * AI analysis task repository, mapping to the ai_analysis_task table.
 */
public interface AiAnalysisTaskRepository extends JpaRepository<AiAnalysisTask, Long> {
    List<AiAnalysisTask> findTop100ByOrderByCreatedAtDesc();
    List<AiAnalysisTask> findByBatchIdOrderByCreatedAtAsc(String batchId);
    boolean existsBySongIdAndStatusIn(Long songId, List<String> statuses);
    Optional<AiAnalysisTask> findFirstBySongIdAndStatusInOrderByCreatedAtDesc(Long songId, List<String> statuses);
    boolean existsByTargetTypeAndTargetIdAndStatusIn(String targetType, Long targetId, List<String> statuses);
}
