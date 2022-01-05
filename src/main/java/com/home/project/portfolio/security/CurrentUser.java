package com.home.project.portfolio.security;

import java.util.Optional;

/**
 * Interface for currently logged user.
 *
 */
public interface CurrentUser {
    Optional<String> getUsername();

    Optional<String> getEmail();

    Optional<String> getToken();

    Optional<String> getPassword();

    Optional<String> getTinkoffToken();

}
