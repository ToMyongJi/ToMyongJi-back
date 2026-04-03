package com.example.tomyongji.domain.my.mapper;

import com.example.tomyongji.domain.admin.dto.MemberDto;
import com.example.tomyongji.domain.my.dto.CollegeAndClubResponseDto;
import com.example.tomyongji.domain.my.dto.MemberRequestDto;
import com.example.tomyongji.domain.my.dto.MyDto;
import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.admin.entity.Member;
import com.example.tomyongji.domain.my.dto.SaveMemberDto;
import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.CollegeDto;
import com.example.tomyongji.domain.receipt.entity.College;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MyMapper {

    MyMapper INSTANCE = Mappers.getMapper(MyMapper.class);

    // User Entity to MyDto
    @Mapping(source = "studentClub.id", target = "studentClubId")
    @Mapping(source = "collegeName", target = "college")
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

    // StudentClub Entity to CollegeAndClubResponseDto
    @Mapping(source = "college", target = "collegeInfo")
    @Mapping(source = "studentClub", target = "clubInfo")
    CollegeAndClubResponseDto toCollegeAndClubResponseDto(StudentClub studentClub);

    @Mapping(source = "id", target = "studentClubId")
    ClubDto toClubDto(StudentClub studentClub);

    @Mapping(source = "id", target = "collegeId")
    CollegeDto toCollegeDto(College college);

}
