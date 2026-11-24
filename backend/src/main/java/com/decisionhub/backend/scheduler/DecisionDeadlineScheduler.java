package com.decisionhub.backend.scheduler;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.NotificationRepository;
import com.decisionhub.backend.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job for voting deadline reminders and automatic closing of expired decisions.
 */
@Component
public class DecisionDeadlineScheduler {

    private final DecisionRepository decisionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public DecisionDeadlineScheduler(
            DecisionRepository decisionRepository,
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.decisionRepository = decisionRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Periodically check for decisions approaching deadlines (within 24 hours)
     * and close expired polls. Runs every 60 seconds.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processDeadlineEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(24);

        // 1. Approaching deadlines: active decisions expiring within 24 hours
        List<Decision> approaching = decisionRepository.findByStatusAndDeadlineBetween(DecisionStatus.ACTIVE, now, windowEnd);
        for (Decision decision : approaching) {
            Long creatorId = decision.getCreator().getId();
            boolean alreadyNotified = notificationRepository.existsByUserIdAndTypeAndReferenceId(
                    creatorId,
                    NotificationType.POLL_DEADLINE_APPROACHING,
                    decision.getId()
            );

            if (!alreadyNotified) {
                notificationService.createNotification(
                        decision.getCreator(),
                        NotificationType.POLL_DEADLINE_APPROACHING,
                        "Voting deadline is approaching for your decision: '" + decision.getTitle() + "'",
                        decision.getId()
                );
            }
        }

        // 2. Expired deadlines: active decisions whose deadline has passed
        List<Decision> expired = decisionRepository.findByStatusAndDeadlineBefore(DecisionStatus.ACTIVE, now);
        for (Decision decision : expired) {
            decision.setStatus(DecisionStatus.CLOSED);
            decisionRepository.save(decision);

            Long creatorId = decision.getCreator().getId();
            boolean alreadyNotified = notificationRepository.existsByUserIdAndTypeAndReferenceId(
                    creatorId,
                    NotificationType.DECISION_CLOSED,
                    decision.getId()
            );

            if (!alreadyNotified) {
                notificationService.createNotification(
                        decision.getCreator(),
                        NotificationType.DECISION_CLOSED,
                        "Voting period has concluded and decision is closed: '" + decision.getTitle() + "'",
                        decision.getId()
                );
            }
        }
    }
}
