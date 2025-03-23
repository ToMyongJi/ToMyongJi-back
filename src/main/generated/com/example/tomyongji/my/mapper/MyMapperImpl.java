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
    date = "2025-03-23T20:39:11+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class MyMapperImpl implements MyMapper {

    @Override
    public MyDto toMyDto(User user) {
        if ( user == null ) {
            return null;
        }

        MyDto.MyDtoBuilder myDto = MyDto.builder();

        Long id = userStudentClubId( user );
        if ( id != null ) {
            myDto.studentClubId( id );
        }
        myDto.college( user.getCollegeName() );
        myDto.name( user.getName() );
        myDto.studentNum( user.getStudentNum() );

        return myDto.build();
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
