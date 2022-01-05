package com.home.project.portfolio.config;

import com.home.project.portfolio.model.entity.UserEntity;
import com.home.project.portfolio.repository.UserRepository;
import com.home.project.portfolio.security.AuthenticatedUser;
import com.home.project.portfolio.security.CurrentUser;
import com.home.project.portfolio.security.UnauthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

/**
 * Security with oath2
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and().oauth2Login()
                .defaultSuccessUrl("/portfolio")
                .and().csrf().disable();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CurrentUser authenticatedUser(OAuth2AuthorizedClientService clientService,
                                         UserRepository userRepository) {

        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth.getClass().isAssignableFrom(OAuth2AuthenticationToken.class))
                .map(auth -> {
                    String accessToken = null;
                    var oauthToken = (OAuth2AuthenticationToken) auth;
                    var clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
                    var email = oauthToken.getPrincipal().getAttributes().get("email").toString();
                    var username = String.valueOf(oauthToken.getPrincipal().getAttributes().get("name"));
                    if (clientRegistrationId.equals("facebook") || clientRegistrationId.equals("google")) {
                        var client = clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());
                        accessToken = client.getAccessToken().getTokenValue();
                    }
                    var tinkoffToken = Optional.ofNullable(userRepository.findByEmail(email))
                            .map(UserEntity::getToken)
                            .orElseGet(() -> {
                                log.warn("Tinkoff token hasn't been found for user: email {}", email);
                                return null;
                            });
                    return (CurrentUser) new AuthenticatedUser(accessToken, username, email, null, tinkoffToken);
                })
                .orElse(UnauthenticatedUser.INSTANCE);
    }
}
