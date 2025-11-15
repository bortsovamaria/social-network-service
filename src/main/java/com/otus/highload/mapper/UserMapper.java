package com.otus.highload.mapper;

import com.otus.highload.model.User;
import com.otus.highload.model.UserRegisterPost200Response;
import com.otus.highload.model.UserRegisterPostRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "id")
    UserRegisterPost200Response toResponse(User user);

    @Mapping(target = "id", ignore = true)
    User toDomain(UserRegisterPostRequest request);
}
