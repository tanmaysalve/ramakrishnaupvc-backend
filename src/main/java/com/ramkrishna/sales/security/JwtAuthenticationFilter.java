package com.ramkrishna.sales.security;

import com.ramkrishna.sales.entity.UserEntity;
import com.ramkrishna.sales.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private final JwtService jwtService;
	private final UserRepository users;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
		this.jwtService = jwtService;
		this.users = users;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {
		String h = req.getHeader("Authorization");
		if (h != null && h.startsWith("Bearer ")) {
			String token = h.substring(7);
			if (jwtService.valid(token)) {
				try {
					String username = jwtService.username(token);
					UserEntity u = users.findByUsername(username).orElse(null);
					if (u != null && u.isActive()) {
						var principal = new AuthenticatedUser(u.getId(), u.getUsername(), u.getDisplayName(),
								u.getRole());
						var auth = new UsernamePasswordAuthenticationToken(principal, null,
								java.util.List
										.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
												"ROLE_" + u.getRole().name())));
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
						SecurityContextHolder.getContext().setAuthentication(auth);
						req.setAttribute("authenticatedUserId", u.getId());
						log.debug("JWT authenticated userId={} role={}", u.getId(), u.getRole());
					} else {
						log.warn("JWT rejected for inactive or unknown user path={}", req.getRequestURI());
					}
				} catch (RuntimeException e) {
					log.error("JWT authentication failed path={}", req.getRequestURI(), e);
				}
			} else {
				log.warn("Invalid or expired JWT path={}", req.getRequestURI());
			}
		}
		chain.doFilter(req, res);
	}
}
