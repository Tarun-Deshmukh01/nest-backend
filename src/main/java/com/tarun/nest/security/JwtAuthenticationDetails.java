package com.tarun.nest.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtAuthenticationDetails {
    private Long userId;
    private String email;
    private String role;
}
