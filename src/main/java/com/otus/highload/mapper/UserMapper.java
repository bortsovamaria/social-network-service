package com.otus.highload.mapper;

import com.otus.highload.model.User;
import com.otus.highload.model.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponse(List<User> user);

}
