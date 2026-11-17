package com.decisionhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "comparison_criteria",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"decision_id", "name"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ComparisonCriteria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double weight = 1.0;
}