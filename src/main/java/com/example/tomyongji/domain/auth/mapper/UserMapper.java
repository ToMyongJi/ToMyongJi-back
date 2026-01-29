package com.example.tomyongji.domain.auth.mapper;

import com.example.tomyongji.domain.auth.dto.UserRequestDto;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
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
