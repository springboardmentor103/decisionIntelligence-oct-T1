package com.decisionhub.report;

import java.time.Instant;

import com.decisionhub.decision.Decision;
import com.decisionhub.user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "decision_reports")
public class DecisionReport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "decision_id")
	private Decision decision;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "requested_by", nullable = false)
	private AppUser requestedBy;

	@Column(name = "report_type", nullable = false, length = 32)
	private String reportType;

	@Column(nullable = false, length = 16)
	private String status = "PENDING";

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected DecisionReport() {
	}

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
