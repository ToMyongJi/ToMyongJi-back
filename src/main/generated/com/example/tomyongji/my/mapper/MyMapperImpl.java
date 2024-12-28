package com.example.tomyongji.my.mapper;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T18:21:31+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class MyMapperImpl implements MyMapper {

    @Override
    public MyDto toMyDto(User user) {
        if ( user == null ) {
            return null;
        }

        MyDto myDto = new MyDto();

        Long id = userStudentClubId( user );
        if ( id != null ) {
            myDto.setStudentClubId( id );
        }
        myDto.setName( user.getName() );
        myDto.setStudentNum( user.getStudentNum() );
        myDto.setCollege( user.getCollege() );

        return myDto;
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

    private Long userStudentClubId(User user) {
        if ( user == null ) {
            return null;
        }
        StudentClub studentClub = user.getStudentClub();
        if ( studentClub == null ) {
            return null;
        }
        Long id = studentClub.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
