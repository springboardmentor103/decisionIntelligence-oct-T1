package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.Ballot;
import com.decisionhub.backend.entity.BallotSelection;
import com.decisionhub.backend.entity.Comment;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMember;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Notification;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.PollType;
import com.decisionhub.backend.entity.Report;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.entity.Vote;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class SchemaPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMemberRepository communityMemberRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private BallotRepository ballotRepository;

    @Autowired
    private BallotSelectionRepository ballotSelectionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private EntityManager entityManager;

    private User createTestUser(String email, String name) {
        User user = User.builder()
                .fullName(name)
                .email(email)
                .password("$2a$10$hashedPasswordSecret")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Should verify that all core tables exist in the database schema")
    void shouldVerifyAllCoreTablesExistInDatabase() {
        @SuppressWarnings("unchecked")
        List<String> tableNames = entityManager.createNativeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()"
        ).getResultList();

        assertThat(tableNames).contains(
                "users",
                "communities",
                "community_members",
                "decisions",
                "options",
                "votes",
                "ballots",
                "ballot_selections",
                "comments",
                "notifications",
                "reports"
        );
    }

    @Test
    @DisplayName("Should persist CommunityMember and reject duplicate membership")
    void shouldPersistCommunityMemberAndRejectDuplicates() {
        User creator = createTestUser("comm.creator@example.com", "Creator");
        User memberUser = createTestUser("comm.member@example.com", "Member");

        Community community = communityRepository.save(Community.builder()
                .name("Green Energy Alliance")
                .description("Promoting renewable energy decisions")
                .creator(creator)
                .build());

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(memberUser)
                .role(CommunityMembershipRole.MEMBER)
                .status(CommunityMembershipStatus.ACTIVE)
                .build();
        CommunityMember saved = communityMemberRepository.save(member);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getJoinedAt()).isNotNull();
        assertThat(communityMemberRepository.existsByCommunityIdAndUserId(community.getId(), memberUser.getId())).isTrue();
        assertThat(communityMemberRepository.findByCommunityId(community.getId())).hasSize(1);

        // Attempting to join the same community again must fail with unique constraint violation
        CommunityMember duplicate = CommunityMember.builder()
                .community(community)
                .user(memberUser)
                .role(CommunityMembershipRole.MODERATOR)
                .status(CommunityMembershipStatus.ACTIVE)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            communityMemberRepository.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should persist Ballot with multiple selections for multiple-choice voting")
    void shouldSupportBallotWithMultipleSelections() {
        User creator = createTestUser("multi.author@example.com", "Author");
        User voter = createTestUser("multi.voter@example.com", "Voter");

        Decision decision = decisionRepository.save(Decision.builder()
                .title("Select your favorite cloud providers")
                .description("Multi-choice selection for cloud deployment")
                .category("INFRASTRUCTURE")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .pollType(PollType.MULTIPLE_CHOICE)
                .creator(creator)
                .build());

        Option optAws = optionRepository.save(Option.builder().decision(decision).title("AWS").build());
        Option optGcp = optionRepository.save(Option.builder().decision(decision).title("GCP").build());
        Option optAzure = optionRepository.save(Option.builder().decision(decision).title("Azure").build());

        Ballot ballot = ballotRepository.save(Ballot.builder()
                .decision(decision)
                .user(voter)
                .build());
        entityManager.flush();

        BallotSelection sel1 = ballotSelectionRepository.save(BallotSelection.builder()
                .ballot(ballot)
                .option(optAws)
                .decision(decision)
                .build());

        BallotSelection sel2 = ballotSelectionRepository.save(BallotSelection.builder()
                .ballot(ballot)
                .option(optGcp)
                .decision(decision)
                .build());
        entityManager.flush();

        assertThat(sel1.getId()).isNotNull();
        assertThat(sel2.getId()).isNotNull();
        assertThat(ballotSelectionRepository.findByBallotId(ballot.getId())).hasSize(2);

        // Attempting to select the same option twice on the same ballot must fail (uk_ballot_selections_ballot_option)
        BallotSelection duplicateSel = BallotSelection.builder()
                .ballot(ballot)
                .option(optAws)
                .decision(decision)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            ballotSelectionRepository.save(duplicateSel);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should enforce one ballot per user per decision at database level")
    void shouldEnforceSingleBallotPerUserPerDecision() {
        User creator = createTestUser("ballot.author@example.com", "Author");
        User voter = createTestUser("ballot.voter@example.com", "Voter");

        Decision decision = decisionRepository.save(Decision.builder()
                .title("Annual Budget Approval")
                .description("Approve or reject municipal budget")
                .category("FINANCE")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .pollType(PollType.SINGLE_CHOICE)
                .creator(creator)
                .build());

        ballotRepository.save(Ballot.builder().decision(decision).user(voter).build());
        entityManager.flush();

        // Second ballot for the same user and decision must be rejected (uk_ballots_user_decision)
        Ballot duplicateBallot = Ballot.builder().decision(decision).user(voter).build();
        assertThrows(DataIntegrityViolationException.class, () -> {
            ballotRepository.save(duplicateBallot);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should reject cross-decision option selection via composite foreign key")
    void shouldRejectCrossDecisionOptionSelection() {
        User creator = createTestUser("cross.creator@example.com", "Creator");
        User voter = createTestUser("cross.voter@example.com", "Voter");

        Decision decisionA = decisionRepository.save(Decision.builder()
                .title("Decision A")
                .description("First decision")
                .category("CAT_A")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .build());

        Decision decisionB = decisionRepository.save(Decision.builder()
                .title("Decision B")
                .description("Second decision")
                .category("CAT_B")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .build());

        Option optionB = optionRepository.save(Option.builder().decision(decisionB).title("Option in B").build());
        Ballot ballotA = ballotRepository.save(Ballot.builder().decision(decisionA).user(voter).build());
        entityManager.flush();

        // Attempting to select Option B under Ballot A (which is for Decision A) must fail at database level
        BallotSelection crossSel = BallotSelection.builder()
                .ballot(ballotA)
                .option(optionB)
                .decision(decisionA)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            ballotSelectionRepository.save(crossSel);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should reject cross-decision parent comment via composite foreign key")
    void shouldRejectCrossDecisionParentComment() {
        User author = createTestUser("cmt.author@example.com", "Author");

        Decision decision1 = decisionRepository.save(Decision.builder()
                .title("Decision 1")
                .description("Topic 1")
                .category("GEN")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(author)
                .build());

        Decision decision2 = decisionRepository.save(Decision.builder()
                .title("Decision 2")
                .description("Topic 2")
                .category("GEN")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(author)
                .build());

        Comment commentInDec1 = commentRepository.save(Comment.builder()
                .decision(decision1)
                .user(author)
                .content("Comment in Decision 1")
                .build());
        entityManager.flush();

        // Attempt to create a reply in Decision 2 pointing to comment in Decision 1
        Comment invalidReply = Comment.builder()
                .decision(decision2)
                .user(author)
                .content("Invalid cross-decision reply")
                .parentComment(commentInDec1)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            commentRepository.save(invalidReply);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should persist Community and Decision with foreign key relationships")
    void shouldPersistCommunityAndDecisionRelationships() {
        User creator = createTestUser("creator@example.com", "Decision Creator");

        Community community = Community.builder()
                .name("Civic Tech Group")
                .description("Community for local civic decision making")
                .creator(creator)
                .build();
        Community savedCommunity = communityRepository.save(community);

        Decision decision = Decision.builder()
                .title("Should we adopt modern lighting?")
                .description("Comprehensive proposal for municipal lighting")
                .category("INFRASTRUCTURE")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .community(savedCommunity)
                .deadline(LocalDateTime.now().plusDays(7))
                .build();
        Decision savedDecision = decisionRepository.save(decision);
        entityManager.flush();

        assertThat(savedCommunity.getId()).isNotNull();
        assertThat(savedDecision.getId()).isNotNull();
        assertThat(savedDecision.getCreator().getId()).isEqualTo(creator.getId());
        assertThat(savedDecision.getCommunity().getId()).isEqualTo(savedCommunity.getId());

        List<Decision> communityDecisions = decisionRepository.findByCommunityId(savedCommunity.getId());
        assertThat(communityDecisions).hasSize(1);
        assertThat(communityDecisions.get(0).getTitle()).isEqualTo("Should we adopt modern lighting?");
    }

    @Test
    @DisplayName("Should persist Decision and Options with cascade and ordering")
    void shouldPersistDecisionWithOptions() {
        User creator = createTestUser("poll.creator@example.com", "Poll Creator");

        Decision decision = Decision.builder()
                .title("Best framework for backend?")
                .description("Vote on the next backend technology")
                .category("TECHNOLOGY")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .build();
        Decision savedDecision = decisionRepository.save(decision);

        Option option1 = Option.builder()
                .title("Spring Boot")
                .description("Java 21 enterprise framework")
                .displayOrder(1)
                .decision(savedDecision)
                .build();

        Option option2 = Option.builder()
                .title("Micronaut")
                .description("Lightweight microservices framework")
                .displayOrder(2)
                .decision(savedDecision)
                .build();

        optionRepository.save(option1);
        optionRepository.save(option2);
        entityManager.flush();

        List<Option> orderedOptions = optionRepository.findByDecisionIdOrderByDisplayOrderAsc(savedDecision.getId());
        assertThat(orderedOptions).hasSize(2);
        assertThat(orderedOptions.get(0).getTitle()).isEqualTo("Spring Boot");
        assertThat(orderedOptions.get(1).getTitle()).isEqualTo("Micronaut");
    }

    @Test
    @DisplayName("Should persist Vote and enforce unique vote per user and decision")
    void shouldPersistVoteAndEnforceSingleVoteConstraint() {
        User voter = createTestUser("voter@example.com", "Active Voter");
        User creator = createTestUser("author@example.com", "Proposal Author");

        Decision decision = Decision.builder()
                .title("Choose City Park Location")
                .description("Vote for location A or B")
                .category("CIVIC")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .build();
        Decision savedDecision = decisionRepository.save(decision);

        Option optionA = Option.builder()
                .title("Location A (North)")
                .decision(savedDecision)
                .displayOrder(1)
                .build();
        Option optionB = Option.builder()
                .title("Location B (South)")
                .decision(savedDecision)
                .displayOrder(2)
                .build();
        optionRepository.save(optionA);
        optionRepository.save(optionB);

        Vote vote1 = Vote.builder()
                .user(voter)
                .decision(savedDecision)
                .option(optionA)
                .build();
        Vote savedVote = voteRepository.save(vote1);
        entityManager.flush();

        assertThat(savedVote.getId()).isNotNull();
        assertThat(savedVote.getVoteDate()).isNotNull();
        assertThat(voteRepository.existsByUserIdAndDecisionId(voter.getId(), savedDecision.getId())).isTrue();
        assertThat(voteRepository.countByOptionId(optionA.getId())).isEqualTo(1L);

        // Attempting to cast a second vote by the same user on the same decision must fail
        Vote duplicateVote = Vote.builder()
                .user(voter)
                .decision(savedDecision)
                .option(optionB)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            voteRepository.save(duplicateVote);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should persist nested comments for threaded discussions")
    void shouldPersistThreadedComments() {
        User user1 = createTestUser("alice@example.com", "Alice");
        User user2 = createTestUser("bob@example.com", "Bob");

        Decision decision = Decision.builder()
                .title("Remote Work Policy")
                .description("Discussion on flexible hours")
                .category("POLICY")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(user1)
                .build();
        decisionRepository.save(decision);

        Comment topComment = Comment.builder()
                .user(user1)
                .decision(decision)
                .content("I strongly support 3 days remote.")
                .build();
        Comment savedTopComment = commentRepository.save(topComment);
        entityManager.flush();

        Comment replyComment = Comment.builder()
                .user(user2)
                .decision(decision)
                .content("Agreed, it provides the best balance.")
                .parentComment(savedTopComment)
                .build();
        Comment savedReply = commentRepository.save(replyComment);
        entityManager.flush();

        List<Comment> topLevelComments = commentRepository.findByDecisionIdAndParentCommentIsNull(decision.getId());
        assertThat(topLevelComments).hasSize(1);
        assertThat(topLevelComments.get(0).getContent()).isEqualTo("I strongly support 3 days remote.");

        List<Comment> replies = commentRepository.findByParentCommentId(savedTopComment.getId());
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getContent()).isEqualTo("Agreed, it provides the best balance.");
        assertThat(replies.get(0).getParentComment().getId()).isEqualTo(savedTopComment.getId());
    }

    @Test
    @DisplayName("Should persist Notification and verify unread count query")
    void shouldPersistNotificationAndQueryUnreadCount() {
        User user = createTestUser("notify.user@example.com", "Notify Target");

        Notification n1 = Notification.builder()
                .user(user)
                .type(NotificationType.VOTE_RECEIVED)
                .message("A new vote has been cast on your decision.")
                .isRead(false)
                .referenceId(101L)
                .build();

        Notification n2 = Notification.builder()
                .user(user)
                .type(NotificationType.DECISION_CLOSED)
                .message("Voting has concluded for your decision.")
                .isRead(true)
                .referenceId(101L)
                .build();

        notificationRepository.save(n1);
        notificationRepository.save(n2);
        entityManager.flush();

        assertThat(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).isEqualTo(1L);
        List<Notification> unreadList = notificationRepository.findByUserIdAndIsRead(user.getId(), false);
        assertThat(unreadList).hasSize(1);
        assertThat(unreadList.get(0).getType()).isEqualTo(NotificationType.VOTE_RECEIVED);
    }

    @Test
    @DisplayName("Should persist Report for decision moderation")
    void shouldPersistReportForDecision() {
        User reporter = createTestUser("reporter@example.com", "Reporter");
        User creator = createTestUser("spam.author@example.com", "Spam Author");

        Decision decision = Decision.builder()
                .title("Spam Advertisement Post")
                .description("Suspicious commercial links")
                .category("OTHER")
                .visibility(DecisionVisibility.PUBLIC)
                .status(DecisionStatus.ACTIVE)
                .creator(creator)
                .build();
        decisionRepository.save(decision);

        Report report = Report.builder()
                .user(reporter)
                .decision(decision)
                .reportType(ReportType.SPAM)
                .description("Violates policy regarding unsolicited promotions.")
                .status(ReportStatus.PENDING)
                .build();
        Report savedReport = reportRepository.save(report);
        entityManager.flush();

        assertThat(savedReport.getId()).isNotNull();
        List<Report> decisionReports = reportRepository.findByDecisionId(decision.getId());
        assertThat(decisionReports).hasSize(1);
        assertThat(decisionReports.get(0).getReportType()).isEqualTo(ReportType.SPAM);
        assertThat(decisionReports.get(0).getStatus()).isEqualTo(ReportStatus.PENDING);
    }
}
