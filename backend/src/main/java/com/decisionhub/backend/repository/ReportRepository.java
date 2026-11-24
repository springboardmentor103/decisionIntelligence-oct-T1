package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Report;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    Page<Report> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Report> findByDecisionCommunityIdOrderByCreatedAtDesc(Long communityId, Pageable pageable);

    Page<Report> findByDecisionCommunityIdAndStatusOrderByCreatedAtDesc(Long communityId, ReportStatus status, Pageable pageable);

    Page<Report> findByDecisionCommunityIdAndReportTypeOrderByCreatedAtDesc(Long communityId, ReportType reportType, Pageable pageable);

    Page<Report> findByDecisionCommunityIdAndStatusAndReportTypeOrderByCreatedAtDesc(Long communityId, ReportStatus status, ReportType reportType, Pageable pageable);

    boolean existsByUserIdAndContentTypeAndContentIdAndStatusIn(Long userId, String contentType, Long contentId, Collection<ReportStatus> statuses);
}
