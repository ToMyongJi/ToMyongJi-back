package com.example.tomyongji.auth.mapper;

import com.example.tomyongji.auth.dto.UserRequestDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "studentClub", target = "studentClub")
    @Mapping(source = "dto.collegeName", target = "collegeName")
    User toUser(UserRequestDto dto, StudentClub studentClub);

    // StudentClub Entity to StudentClub Dto



}
