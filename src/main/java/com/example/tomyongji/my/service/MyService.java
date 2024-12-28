package com.example.tomyongji.my.service;

import static com.example.tomyongji.validation.ErrorMsg.EXISTING_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.my.mapper.MyMapper;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MyMapper myMapper;

    @Autowired
    public MyService(UserRepository userRepository,
        MemberRepository memberInfoRepository, EmailVerificationRepository emailVerificationRepository,
        MemberRepository memberRepository, MyMapper myMapper) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.myMapper = myMapper;
    }

    // mapper 사용 추천
    public MyDto getMyInfo(Long userId) {
        Optional<User> userById = userRepository.findById(userId);
        if (userById.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        User user = userById.get();
        if (user.getStudentClub() == null) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        return myMapper.toMyDto(user);
    }



    public List<MemberDto> getMembers(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        StudentClub studentClub = user.get().getStudentClub();
        List<Member> members = memberRepository.findByStudentClub(studentClub);
        List<MemberDto> memberDtos = new ArrayList<>();
        for (Member member : members) {
            memberDtos.add(myMapper.toMemberDto(member));
        }
        return memberDtos;
    }

    public void saveMember(SaveMemberDto memberDto) {
        Optional<User> user = userRepository.findById(memberDto.getPresidentUserId());
        if (user.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }

        if (memberRepository.existsByStudentNum(memberDto.getStudentNum())) {
            throw new CustomException(EXISTING_USER, 400);  // 중복 학번 예외 처리
        }
        Optional<StudentClub> studentClub = Optional.ofNullable(user.get().getStudentClub()); //회장 유저 정보로 학생회 찾기
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        //멤버 생성 후
        //받은 dto의 정보를 멤버에 삽입
        //멤버의 학생회 정보를 따로 삽입
        //해당 멤버를 레포지터리에 저장
        Member member = myMapper.toMemberEntity(memberDto);
        member.setStudentClub(studentClub.get());
        memberRepository.save(member);
    }

    public MemberDto deleteMember(Long deleteId) {

        Optional<Member> member = memberRepository.findById(deleteId);
        if (member.isEmpty()) {
            throw new CustomException(NOT_FOUND_MEMBER, 400);
        }
        MemberDto memberDto = myMapper.toMemberDto(member.get()); //삭제된 멤버 정보를 보여주기 위한 반환값

        //멤버 등록을 해도 유저가 없을 수 있음
        Optional<User> user = Optional.ofNullable(
            userRepository.findByStudentNum(member.get().getStudentNum()));
        //유저가 있다면 유저의 메일과 유저를 삭제
        if (user.isPresent()) {
            emailVerificationRepository.deleteByEmail(user.get().getEmail());
            userRepository.delete(user.get());
        }
        //등록된 멤버 정보도 삭제
        memberRepository.deleteById(deleteId);
        return memberDto;
    }
}
