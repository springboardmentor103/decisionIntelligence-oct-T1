package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.PollResultDtos;

public interface PollResultService {

    PollResultDtos.PollResultResponse getPollResults(
            Long decisionId,
            String email
    );
}