package com.decisionhub.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisionhub.auth.AuthDtos;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@GetMapping("/status")
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	public AuthDtos.MessageResponse status() {
		return new AuthDtos.MessageResponse("Administrator access verified.");
	}
}
