package com.example.tomyongji.admin.mapper;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T18:21:31+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class AdminMapperImpl implements AdminMapper {

    @Override
    public PresidentDto toPresidentDto(President president) {
        if ( president == null ) {
            return null;
        }

        PresidentDto presidentDto = new PresidentDto();

        presidentDto.setStudentNum( president.getStudentNum() );
        presidentDto.setName( president.getName() );

        return presidentDto;
    }

    @Override
    public President toPresidentEntity(PresidentDto presidentDto) {
        if ( presidentDto == null ) {
            return null;
        }

        President president = new President();

        president.setStudentNum( presidentDto.getStudentNum() );
        president.setName( presidentDto.getName() );

        return president;
    }

    @Override
    public MemberDto toMemberDto(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberDto memberDto = new MemberDto();

        memberDto.setMemberId( member.getId() );
        memberDto.setStudentNum( member.getStudentNum() );
        memberDto.setName( member.getName() );

        return memberDto;
    }

    @Override
    public MemberDto toMemberDto(User user) {
        if ( user == null ) {
            return null;
        }

        MemberDto memberDto = new MemberDto();

        memberDto.setStudentNum( user.getStudentNum() );
        memberDto.setName( user.getName() );

        return memberDto;
    }

    @Override
    public Member toMemberEntity(MemberDto memberDto) {
        if ( memberDto == null ) {
            return null;
        }

        Member member = new Member();

        member.setStudentNum( memberDto.getStudentNum() );
        member.setName( memberDto.getName() );

        return member;
    }

    @Override
    public Member toMemberEntity(MemberRequestDto memberRequestDto) {
        if ( memberRequestDto == null ) {
            return null;
        }

        Member member = new Member();

        member.setStudentNum( memberRequestDto.getStudentNum() );
        member.setName( memberRequestDto.getName() );

        return member;
    }

    @Override
    public Member toMemberEntity(User user) {
        if ( user == null ) {
            return null;
        }

        Member member = new Member();

        member.setId( user.getId() );
        member.setStudentNum( user.getStudentNum() );
        member.setName( user.getName() );
        member.setStudentClub( user.getStudentClub() );

        return member;
    }

    @Override
    public Member toMemberEntity(SaveMemberDto saveMemberDto) {
        if ( saveMemberDto == null ) {
            return null;
        }

        Member member = new Member();

        member.setStudentNum( saveMemberDto.getStudentNum() );
        member.setName( saveMemberDto.getName() );

        return member;
    }

    @Override
    public Member toMemberEntity(AdminSaveMemberDto adminSaveMemberDto) {
        if ( adminSaveMemberDto == null ) {
            return null;
        }

        Member member = new Member();

        member.setStudentNum( adminSaveMemberDto.getStudentNum() );
        member.setName( adminSaveMemberDto.getName() );

        return member;
    }
}
