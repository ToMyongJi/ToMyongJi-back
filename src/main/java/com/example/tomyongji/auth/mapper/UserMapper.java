package com.example.tomyongji.auth.mapper;

import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "studentClub", target = "studentClub")
    @Mapping(source = "dto.college", target = "college")
    User toUser(UserRequsetDto dto, StudentClub studentClub);

    // StudentClub Entity to StudentClub Dto



}
