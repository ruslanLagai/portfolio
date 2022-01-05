package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.entity.UserEntity;
import com.home.project.portfolio.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/user", produces = MediaType.TEXT_PLAIN_VALUE)
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserEntity user) {
        userRepository.save(user);
        return ResponseEntity.ok().body("User added");
    }
}
