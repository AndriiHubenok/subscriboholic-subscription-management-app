package com.anhub.subscriboholic.mapper;

import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.User;
import com.anhub.subscriboholic.model.enumerated.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {

        userMapper = org.mapstruct.factory.Mappers.getMapper(UserMapper.class);
    }

    @Test
    void shouldMapEntityToDto() {

        User user = new User();
        user.setId(1);
        user.setUsername("Adam Jensen");
        user.setEmail("adam@test.com");
        user.setRole(UserRole.USER);

        UserDTO userDTO = userMapper.toDTO(user);

        assertNotNull(userDTO);
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getUsername(), userDTO.getUsername());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getRole(), userDTO.getRole());
    }

    @Test
    void shouldMapCreateRequestToEntity() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("Adam Jensen");
        request.setPassword("SecurePass123!");
        request.setEmail("adam@test.com");

        User user = userMapper.toEntity(request);

        assertNotNull(user);
        assertNull(user.getId());
        assertEquals(request.getUsername(), user.getUsername());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(request.getPassword(), user.getPassword());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {

        UserDTO userDTO = userMapper.toDTO(null);

        assertNull(userDTO);
    }

    @Test
    void shouldReturnNullWhenRequestIsNull() {

        User user = userMapper.toEntity(null);

        assertNull(user);
    }
}
