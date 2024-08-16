package com.example.tomyongji.my.service;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.admin.repository.MemberInfoRepository;
import com.example.tomyongji.admin.service.AdminService;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MyDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
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

    @Autowired
    public MyService(UserRepository userRepository, StudentClubRepository studentClubRepository,
        AdminService adminService, MemberInfoRepository memberInfoRepository) {
        this.userRepository = userRepository;
        this.studentClubRepository = studentClubRepository;
        this.adminService = adminService;
        this.memberInfoRepository = memberInfoRepository;
    }

    public MyDto getMyInfo(Long id) {
        Optional<User> userById = userRepository.findById(id);
        if (userById.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        User user = userById.get();

        MyDto myDto = new MyDto();
        myDto.setName(user.getName());
        myDto.setStudentNum(user.getStudentNum());
        myDto.setStudentClubName(user.getStudentClub().getStudentClubName());

        return myDto;
    }

    public void updateMyInfo(Long id, String studentNum) {
        Optional<User> userById = userRepository.findById(id);
        if (userById.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        User user = userById.get();
        user.setStudentNum(studentNum);
        userRepository.save(user);
    }

    public List<MemberDto> getMembers(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다.");
        }
        StudentClub studentClub = user.get().getStudentClub();
        List<MemberInfo> memberInfos = memberInfoRepository.findByStudentClub(studentClub);
        List<MemberDto> memberDtos = new ArrayList<>();
        for (MemberInfo memberInfo : memberInfos) {
            MemberDto memberDto = new MemberDto();
            memberDto.setStudentNum(memberInfo.getStudentNum());
            memberDto.setName(memberInfo.getName());
            memberDtos.add(memberDto);
        }
        return memberDtos;
    }

    public MemberDto saveMember(Long id, MemberDto memberDto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다.");
        }
        StudentClub studentClub = studentClubRepository.findByUsers(user.get());
        adminService.saveMember(studentClub.getId(), memberDto);
        return memberDto;
    }

    public void deleteMember(Long deleteId) {
        //MemberInfo와 User에서 삭제
        Optional<User> user = userRepository.findById(deleteId);
        if (user.isEmpty()) {
            throw new RuntimeException("유저를 찾을 수 없습니다.");
        }
        MemberInfo memberInfo = memberInfoRepository.findByStudentNum(user.get().getStudentNum());
        memberInfoRepository.delete(memberInfo);
        userRepository.delete(user.get());
    }
}
