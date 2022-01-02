package com.home.project.portfolio.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity(name = "USER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @GeneratedValue(strategy= GenerationType.AUTO)
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String token;
}
