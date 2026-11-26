package com.decisionhub.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures composite foreign key constraints required for cross-decision referential integrity
 * are present in MySQL, since Hibernate's ddl-auto=update does not auto-generate multi-column foreign keys.
 */
@Component
public class DatabaseConstraintInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        addConstraintIfNotExists(
            "comments",
            "fk_comments_parent_composite",
            "ALTER TABLE comments ADD CONSTRAINT fk_comments_parent_composite " +
            "FOREIGN KEY (parent_comment_id, decision_id) REFERENCES comments (id, decision_id) ON DELETE CASCADE"
        );

        addConstraintIfNotExists(
            "ballot_selections",
            "fk_ballot_selections_opt_dec",
            "ALTER TABLE ballot_selections ADD CONSTRAINT fk_ballot_selections_opt_dec " +
            "FOREIGN KEY (option_id, decision_id) REFERENCES options (id, decision_id) ON DELETE CASCADE"
        );

        addConstraintIfNotExists(
            "ballot_selections",
            "fk_ballot_selections_bal_dec",
            "ALTER TABLE ballot_selections ADD CONSTRAINT fk_ballot_selections_bal_dec " +
            "FOREIGN KEY (ballot_id, decision_id) REFERENCES ballots (id, decision_id) ON DELETE CASCADE"
        );
    }

    private void addConstraintIfNotExists(String tableName, String constraintName, String alterSql) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND CONSTRAINT_NAME = ?",
                Integer.class,
                constraintName
            );

            if (count == null || count == 0) {
                log.info("Auto-applying missing database constraint: {} on {}", constraintName, tableName);
                jdbcTemplate.execute(alterSql);
                log.info("Successfully applied database constraint: {}", constraintName);
            }
        } catch (Exception e) {
            log.warn("Could not auto-apply constraint {}: {}", constraintName, e.getMessage());
        }
    }
}
