package com.skillvault.backend.Domain.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER("ROLE_USER"),
    EVALUATOR("ROLE_EVALUATOR"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    public List<String> getPermissions() {
        return switch (this) {
            case ADMIN -> List.of("ROLE_ADMIN", "ROLE_EVALUATOR", "ROLE_CLIENT");
            case EVALUATOR -> List.of("ROLE_EVALUATOR","ROLE_CLIENT");
            case USER -> List.of("ROLE_USER");
        };
    }

}
