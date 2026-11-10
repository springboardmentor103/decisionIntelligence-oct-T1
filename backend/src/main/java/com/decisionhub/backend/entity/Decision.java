package com.decisionhub.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a collective decision or community poll.
 */
@Entity
@Table(
    name = "decisions",
    indexes = {
        @Index(name = "idx_decisions_creator", columnList = "creator_id"),
        @Index(name = "idx_decisions_community", columnList = "community_id"),
        @Index(name = "idx_decisions_status", columnList = "status"),
        @Index(name = "idx_decisions_category", columnList = "category")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private DecisionVisibility visibility = DecisionVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DecisionStatus status = DecisionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "poll_type", nullable = false, length = 30)
    @Builder.Default
    private PollType pollType = PollType.SINGLE_CHOICE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "creator_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_decisions_creator")
    )
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "community_id",
        foreignKey = @ForeignKey(name = "fk_decisions_community")
    )
    private Community community;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Option> options = new ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ballot> ballots = new ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();
}

