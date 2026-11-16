package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.OptionBatchRequestDto;
import com.decisionhub.backend.dto.OptionRequestDto;
import com.decisionhub.backend.dto.OptionResponseDto;
import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.OptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;

    public OptionServiceImpl(
            OptionRepository optionRepository,
            DecisionRepository decisionRepository,
            UserRepository userRepository) {
        this.optionRepository = optionRepository;
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<OptionResponseDto> addOptions(Long decisionId, OptionBatchRequestDto request, String currentUserEmail) {
        if (request == null || request.options() == null) {
            throw new BadRequestException("At least one option must be provided");
        }
        return addOptions(decisionId, request.options(), currentUserEmail);
    }

    @Override
    public List<OptionResponseDto> addOptions(Long decisionId, List<OptionRequestDto> options, String currentUserEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        if (options == null || options.isEmpty()) {
            throw new BadRequestException("At least one option must be provided");
        }

        int maxOrder = decision.getOptions().stream()
                .mapToInt(opt -> opt.getDisplayOrder() != null ? opt.getDisplayOrder() : 0)
                .max()
                .orElse(-1);

        int nextOrder = maxOrder + 1;
        List<Option> newOptions = new ArrayList<>();

        for (OptionRequestDto optDto : options) {
            if (optDto.title() == null || optDto.title().isBlank()) {
                throw new BadRequestException("Option title cannot be blank");
            }
            Option option = Option.builder()
                    .decision(decision)
                    .title(optDto.title().trim())
                    .description(optDto.description() != null ? optDto.description().trim() : null)
                    .displayOrder(optDto.displayOrder() != null ? optDto.displayOrder() : nextOrder++)
                    .build();
            newOptions.add(option);
            decision.getOptions().add(option);
        }

        List<Option> saved = optionRepository.saveAll(newOptions);
        return saved.stream().map(OptionResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionResponseDto> getOptionsByDecisionId(Long decisionId, String currentUserEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        validateAccess(decision, currentUserEmail);

        List<Option> options = optionRepository.findByDecisionIdOrderByDisplayOrderAsc(decisionId);
        return options.stream().map(OptionResponseDto::fromEntity).toList();
    }

    @Override
    public OptionResponseDto updateOption(Long decisionId, Long optionId, OptionRequestDto request, String currentUserEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option", "id", optionId));

        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        if (request.title() != null && !request.title().isBlank()) {
            option.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            option.setDescription(request.description().trim());
        }
        if (request.displayOrder() != null) {
            option.setDisplayOrder(request.displayOrder());
        }

        Option updated = optionRepository.save(option);
        return OptionResponseDto.fromEntity(updated);
    }

    @Override
    public void deleteOption(Long decisionId, Long optionId, String currentUserEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option", "id", optionId));

        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        decision.getOptions().remove(option);
        optionRepository.delete(option);
    }

    private void validateOptionBelongsToDecision(Option option, Long decisionId) {
        if (option.getDecision() == null || !option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException(
                    String.format("Option with id %d does not belong to decision %d", option.getId(), decisionId)
            );
        }
    }

    private User getAuthenticatedUser(String email) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }
        return user;
    }

    private void validateAccess(Decision decision, String currentUserEmail) {
        if (decision.getVisibility() == DecisionVisibility.PUBLIC) {
            return;
        }

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new UnauthorizedException("Authentication required to access options for this private decision");
        }

        User user = getAuthenticatedUser(currentUserEmail);
        boolean isCreator = decision.getCreator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view options for this private decision");
        }
    }

    private void validateModificationPermission(Decision decision, User currentUser) {
        boolean isCreator = decision.getCreator().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify options for this decision");
        }
    }
}
