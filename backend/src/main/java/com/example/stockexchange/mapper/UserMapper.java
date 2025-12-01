package com.example.stockexchange.mapper;

import com.example.stockexchange.dto.UserDto;
import com.example.stockexchange.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User map(UserDto userDto);

    UserDto userToUserDTO(User user);

    List<UserDto> map(List<User> users);
}
