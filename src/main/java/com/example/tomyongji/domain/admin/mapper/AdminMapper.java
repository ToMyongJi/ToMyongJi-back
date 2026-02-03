package com.example.tomyongji.domain.admin.mapper;

import com.example.tomyongji.domain.admin.dto.MemberDto;
import com.example.tomyongji.domain.admin.dto.PresidentDto;
import com.example.tomyongji.domain.admin.entity.Member;
import com.example.tomyongji.domain.admin.entity.President;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.domain.my.dto.MemberRequestDto;
import com.example.tomyongji.domain.my.dto.SaveMemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminMapper INSTANCE = Mappers.getMapper(AdminMapper.class);

    // President Entity to DTO
    PresidentDto toPresidentDto(President president);

    // President DTO to Entity
    President toPresidentEntity(PresidentDto presidentDto);

    // Member Entity to DTO
    @Mapping(source = "id", target = "memberId")
    MemberDto toMemberDto(Member member);

    // User Entity to DTO
    MemberDto toMemberDto(User user);

    // Member DTO to Entity
    Member toMemberEntity(MemberDto memberDto);

    // Member Request DTO to Entity
    Member toMemberEntity(MemberRequestDto memberRequestDto);

    // User to Member Entity
    Member toMemberEntity(User user);

    // SaveMemberDto to Member Entity
    Member toMemberEntity(SaveMemberDto saveMemberDto);

    // AdminSaveMemberDto to Member Entity
    Member toMemberEntity(AdminSaveMemberDto adminSaveMemberDto);



}
