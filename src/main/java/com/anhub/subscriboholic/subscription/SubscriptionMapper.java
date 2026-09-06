package com.anhub.subscriboholic.subscription;

import com.anhub.subscriboholic.subscription.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.subscription.dto.SubscriptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface SubscriptionMapper {

    @Mapping(source = "user.id", target = "userId")
    SubscriptionDTO toDTO(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Subscription toEntity(CreateSubscriptionRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(CreateSubscriptionRequest dto, @MappingTarget Subscription entity);
}
