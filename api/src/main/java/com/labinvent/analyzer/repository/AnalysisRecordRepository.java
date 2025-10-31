package com.labinvent.analyzer.repository;

import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecord, Long> {

    @Query("SELECT r FROM AnalysisRecord r ORDER BY r.uploadedAt ASC")
    List<AnalysisRecord> findOldest(Pageable pageable);

    Optional<AnalysisRecord> findFirstByStatusOrderByUploadedAtAsc(AnalysisRecordStatus analysisRecordStatus);
}