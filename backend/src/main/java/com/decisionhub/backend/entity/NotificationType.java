package com.decisionhub.backend.entity;

/**
 * Categorization of user notifications across discussions, feedback, voting, and communities.
 */
public enum NotificationType {
    // Voting & Decisions
    VOTE_RECEIVED,
    POLL_DEADLINE_APPROACHING,
    DECISION_CLOSED,

    // Discussions
    COMMENT_RECEIVED,
    COMMENT_REPLY,

    // Feedback
    FEEDBACK_RECEIVED,
    FEEDBACK_STATUS_CHANGED,

    // Community Membership
    MEMBERSHIP_REQUESTED,
    MEMBERSHIP_APPROVED,
    MEMBERSHIP_REJECTED,
    MEMBER_REMOVED,
    COMMUNITY_OWNERSHIP_TRANSFERRED,

    // System
    SYSTEM
}
