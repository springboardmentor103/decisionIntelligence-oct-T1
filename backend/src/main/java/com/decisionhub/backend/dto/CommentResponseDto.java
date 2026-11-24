package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Comment;

import java.time.LocalDateTime;

/**
 * Response DTO representing a discussion comment or reply.
 */
public record CommentResponseDto(
    Long id,
    Long decisionId,
    CommentAuthorDto author,
    String content,
    Long parentCommentId,
    boolean isDeleted,
    long replyCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponseDto fromEntity(Comment comment, long replyCount) {
        if (comment == null) {
            return null;
        }

        String displayedContent = comment.isDeleted()
                ? "[This comment has been deleted]"
                : comment.getContent();

        return new CommentResponseDto(
            comment.getId(),
            comment.getDecision() != null ? comment.getDecision().getId() : null,
            CommentAuthorDto.fromEntity(comment.getUser()),
            displayedContent,
            comment.getParentComment() != null ? comment.getParentComment().getId() : null,
            comment.isDeleted(),
            replyCount,
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
