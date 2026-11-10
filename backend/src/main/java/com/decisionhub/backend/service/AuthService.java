
package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.RegisterRequest;
import com.decisionhub.backend.dto.LoginRequest;
import com.decisionhub.backend.dto.AuthResponse;

public interface AuthService {

    Long registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest request);
}
