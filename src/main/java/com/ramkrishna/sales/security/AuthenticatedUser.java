package com.ramkrishna.sales.security;

import com.ramkrishna.sales.entity.Enums.Role;

public record AuthenticatedUser(String id, String username, String displayName, Role role) {
}
