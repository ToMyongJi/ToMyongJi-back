package com.example.tomyongji.my.mapper;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.my.dto.SaveMemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MyMapper {

    MyMapper INSTANCE = Mappers.getMapper(MyMapper.class);

    // User Entity to MyDto
    @Mapping(source = "studentClub.id", target = "studentClubId")
    MyDto toMyDto(User user);

    // MemberRequestDto to Member Entity
    Member toMemberEntity(MemberRequestDto memberRequestDto);

    // SaveMemberDto to Member Entity
    Member toMemberEntity(SaveMemberDto saveMemberDto);

    // User Entity to MemberDto
    MemberDto toMemberDto(User user);

    // Member Entity to MemberDto
    @Mapping(source = "id", target = "memberId")
    MemberDto toMemberDto(Member member);

}
