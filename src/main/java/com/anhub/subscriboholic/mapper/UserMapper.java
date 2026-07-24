package com.anhub.subscriboholic.mapper;

import com.anhub.subscriboholic.model.dto.CreateUserRequest;
import com.anhub.subscriboholic.model.dto.UserDTO;
import com.anhub.subscriboholic.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest request);
}
