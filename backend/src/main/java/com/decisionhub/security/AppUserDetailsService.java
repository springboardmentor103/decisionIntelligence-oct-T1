package com.decisionhub.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.decisionhub.user.AppUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
	private final AppUserRepository users;

	public AppUserDetailsService(AppUserRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		var user = users.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found."));
		return User.withUsername(user.getEmail())
				.password(user.getPasswordHash())
				.authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
				.build();
	}
}
