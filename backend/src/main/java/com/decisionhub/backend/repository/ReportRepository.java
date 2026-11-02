package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Report;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for managing Report entities.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByDecisionId(Long decisionId);

    List<Report> findByUserId(Long userId);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReportType(ReportType reportType);
}

