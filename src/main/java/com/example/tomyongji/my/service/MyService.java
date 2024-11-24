package com.example.tomyongji.my.service;

import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_USER;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.admin.repository.MemberInfoRepository;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.my.dto.MyDto;
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
    private final StudentClubRepository studentClubRepository;
    private final AdminService adminService;
    private final MemberInfoRepository memberInfoRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Autowired
    public MyService(UserRepository userRepository, StudentClubRepository studentClubRepository,
                     AdminService adminService, MemberInfoRepository memberInfoRepository,
                     EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.studentClubRepository = studentClubRepository;
        this.adminService = adminService;
        this.memberInfoRepository = memberInfoRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    public MyDto getMyInfo(Long id) {
        Optional<User> userById = userRepository.findById(id);
        if (userById.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        User user = userById.get();

        MyDto myDto = new MyDto();
        myDto.setName(user.getName());
        myDto.setStudentNum(user.getStudentNum());
        myDto.setCollege(user.getCollege());
        myDto.setStudentClubId(user.getStudentClub().getId());

        return myDto;
    }


    public List<MemberDto> getMembers(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        StudentClub studentClub = user.get().getStudentClub();
        List<MemberInfo> memberInfos = memberInfoRepository.findByStudentClub(studentClub);
        List<MemberDto> memberDtos = new ArrayList<>();
        for (MemberInfo memberInfo : memberInfos) {
            memberDtos.add(convertToMemberDto(memberInfo));
        }
        return memberDtos;
    }

    public void saveMember(Long id, MemberRequestDto memberDto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new CustomException(NOT_FOUND_USER, 400);
        }
        StudentClub studentClub = studentClubRepository.findByUsers(user.get());
        adminService.saveMember(studentClub.getId(), memberDto);
    }

    public MemberDto deleteMember(Long deleteId) {

        Optional<MemberInfo> memberInfo = memberInfoRepository.findById(deleteId);
        if (memberInfo.isEmpty()) {
            throw new CustomException(NOT_FOUND_MEMBER, 400);
        }
        MemberDto memberDto = convertToMemberDto(memberInfo.get());

        //멤버 등록을 해도 유저가 없을 수 있음
        Optional<User> user = Optional.ofNullable(
            userRepository.findByStudentNum(memberInfo.get().getStudentNum()));
        //유저가 있다면 유저의 메일과 유저를 삭제
        if (user.isPresent()) {
            emailVerificationRepository.deleteByEmail(user.get().getEmail());
            userRepository.delete(user.get());
        }
        //등록된 멤버 정보도 삭제
        memberInfoRepository.deleteById(deleteId);
        return memberDto;
    }

    private MemberDto convertToMemberDto(MemberInfo memberInfo) {
        MemberDto memberDto = new MemberDto();
        memberDto.setStudentNum(memberInfo.getStudentNum());
        memberDto.setName(memberInfo.getName());
        memberDto.setMemberId(memberInfo.getId());
        return memberDto;
    }
}
