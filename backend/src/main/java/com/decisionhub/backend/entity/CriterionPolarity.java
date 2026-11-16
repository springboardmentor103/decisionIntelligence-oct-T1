package com.decisionhub.backend.entity;

/**
 * Indicates whether a higher raw value is preferable (e.g. benefits, quality)
 * or a lower raw value is preferable (e.g. cost, risk, time required).
 */
public enum CriterionPolarity {
    HIGHER_IS_BETTER,
    LOWER_IS_BETTER
}
