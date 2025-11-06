package com.labinvent.analyzer.repository;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    @Query("SELECT r FROM AnalysisResult r ORDER BY r.uploadedAt ASC")
    List<AnalysisResult> findOldest(Pageable pageable);

    Optional<AnalysisResult> findFirstByStatusOrderByUploadedAtAsc(AnalysisResultStatus analysisResultStatus);

    List<AnalysisResult> findAllByStatus(AnalysisResultStatus status);
}