package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CreateFeedbackRequest;
import com.decisionhub.backend.dto.FeedbackResponseDto;
import com.decisionhub.backend.dto.FeedbackSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateFeedbackRequest;
import com.decisionhub.backend.dto.UpdateFeedbackStatusRequest;
import com.decisionhub.backend.entity.FeedbackType;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Feedback Management operations.
 */
public interface FeedbackService {

    FeedbackResponseDto submitFeedback(Long decisionId, CreateFeedbackRequest request, String userEmail);

    PagedResponseDto<FeedbackResponseDto> getFeedbackList(Long decisionId, FeedbackType feedbackType, Pageable pageable, String userEmail);

    FeedbackResponseDto getFeedbackById(Long feedbackId, String userEmail);

    FeedbackResponseDto updateFeedback(Long feedbackId, UpdateFeedbackRequest request, String userEmail);

    void deleteFeedback(Long feedbackId, String userEmail);

    FeedbackResponseDto updateFeedbackStatus(Long feedbackId, UpdateFeedbackStatusRequest request, String userEmail);

    FeedbackSummaryResponseDto getFeedbackSummary(Long decisionId, String userEmail);
}
