package com.home.project.portfolio.security;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class AuthenticatedUser implements CurrentUser {
    private final String token;
    private final String username;
    private final String email;
    private final String password;
    private final String tinkoffApiToken;

    @Override
    public Optional<String> getUsername() {
        return Optional.of(username);
    }

    @Override
    public Optional<String> getEmail() {
        return Optional.of(email);
    }

    @Override
    public Optional<String> getToken() {
        return Optional.of(token);
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public Optional<String> getTinkoffToken() {
        return Optional.ofNullable(tinkoffApiToken);
    }
}