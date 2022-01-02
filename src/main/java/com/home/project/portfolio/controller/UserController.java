package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.entity.UserEntity;
import com.home.project.portfolio.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity addUser(@RequestBody UserEntity user) {

        Optional.ofNullable(userRepository.findByUsername(user.getUsername()))
                .orElseGet(() -> userRepository.save(user));
        return null;
    }
}
