package com.home.project.portfolio.security;

import java.util.Optional;

/**
 * Implementation of unauthenticated user.
 *
 */
public class UnauthenticatedUser implements CurrentUser {
    public static final CurrentUser INSTANCE = new UnauthenticatedUser();

    @Override
    public Optional<String> getUsername() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getEmail() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getToken() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTinkoffToken() {
        return Optional.empty();
    }
}
