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
    date = "2025-03-08T23:47:38+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class AdminMapperImpl implements AdminMapper {

    @Override
    public PresidentDto toPresidentDto(President president) {
        if ( president == null ) {
            return null;
        }

        PresidentDto.PresidentDtoBuilder presidentDto = PresidentDto.builder();

        presidentDto.studentNum( president.getStudentNum() );
        presidentDto.name( president.getName() );

        return presidentDto.build();
    }

    @Override
    public President toPresidentEntity(PresidentDto presidentDto) {
        if ( presidentDto == null ) {
            return null;
        }

        President.PresidentBuilder president = President.builder();

        president.studentNum( presidentDto.getStudentNum() );
        president.name( presidentDto.getName() );

        return president.build();
    }

    @Override
    public MemberDto toMemberDto(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberDto.MemberDtoBuilder memberDto = MemberDto.builder();

        memberDto.memberId( member.getId() );
        memberDto.studentNum( member.getStudentNum() );
        memberDto.name( member.getName() );

        return memberDto.build();
    }

    @Override
    public MemberDto toMemberDto(User user) {
        if ( user == null ) {
            return null;
        }

        MemberDto.MemberDtoBuilder memberDto = MemberDto.builder();

        memberDto.studentNum( user.getStudentNum() );
        memberDto.name( user.getName() );

        return memberDto.build();
    }

    @Override
    public Member toMemberEntity(MemberDto memberDto) {
        if ( memberDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.studentNum( memberDto.getStudentNum() );
        member.name( memberDto.getName() );

        return member.build();
    }

    @Override
    public Member toMemberEntity(MemberRequestDto memberRequestDto) {
        if ( memberRequestDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.studentNum( memberRequestDto.getStudentNum() );
        member.name( memberRequestDto.getName() );

        return member.build();
    }

    @Override
    public Member toMemberEntity(User user) {
        if ( user == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.id( user.getId() );
        member.studentNum( user.getStudentNum() );
        member.name( user.getName() );
        member.studentClub( user.getStudentClub() );

        return member.build();
    }

    @Override
    public Member toMemberEntity(SaveMemberDto saveMemberDto) {
        if ( saveMemberDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.id( saveMemberDto.getId() );
        member.studentNum( saveMemberDto.getStudentNum() );
        member.name( saveMemberDto.getName() );

        return member.build();
    }

    @Override
    public Member toMemberEntity(AdminSaveMemberDto adminSaveMemberDto) {
        if ( adminSaveMemberDto == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.studentNum( adminSaveMemberDto.getStudentNum() );
        member.name( adminSaveMemberDto.getName() );

        return member.build();
    }
}
