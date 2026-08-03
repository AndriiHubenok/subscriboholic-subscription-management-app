package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import com.anhub.subscriboholic.security.JwtService;
import com.anhub.subscriboholic.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "B.J.", roles = {"ADMIN"})
    void shouldCreateUserAndReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Geralt");
        request.setPassword("TemeriaIsTheBest69!");
        request.setEmail("shanifan@gmail.com");

        UserDTO mockResponse = new UserDTO();
        mockResponse.setId(1);
        mockResponse.setUsername("Geralt");
        mockResponse.setEmail("shanifan@gmail.com");
        mockResponse.setRole(UserRole.USER);
        mockResponse.setCreatedAt(LocalDateTime.now());
        mockResponse.setUpdatedAt(LocalDateTime.now());

        Mockito.when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())

                .andExpect(header().string("Location", "http://localhost/users/1"))

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Geralt"))
                .andExpect(jsonPath("$.email").value("shanifan@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @WithMockUser(username = "B.J.", roles = {"ADMIN"})
    void shouldGetUserByIdAndReturn200() throws Exception {
        UserDTO mockUser = new UserDTO();
        mockUser.setId(1);
        mockUser.setUsername("Geralt");
        mockUser.setEmail("TemeriaIsTheBest69!");
        mockUser.setRole(UserRole.USER);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setUpdatedAt(LocalDateTime.now());

        Mockito.when(userService.getUserById(1)).thenReturn(mockUser);

        mockMvc.perform(get("/users/{id}", 1)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Geralt"))
                .andExpect(jsonPath("$.email").value("TemeriaIsTheBest69!"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @Test
    @WithMockUser(username = "B.J.", roles = {"ADMIN"})
    void shouldReturn204WhenUserDeletedSuccessfully() throws Exception {
        Mockito.when(userService.deleteUserById(1)).thenReturn(true);

        mockMvc.perform(delete("/users/{id}", 1).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "B.J.", roles = {"ADMIN"})
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        Mockito.when(userService.deleteUserById(99)).thenReturn(false);

        mockMvc.perform(delete("/users/{id}", 99).with(csrf()))
                .andExpect(status().isNotFound());
    }
}