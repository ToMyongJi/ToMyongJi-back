package com.example.tomyongji.admin.service;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.admin.entity.PresidentInfo;
import com.example.tomyongji.admin.repository.MemberInfoRepository;
import com.example.tomyongji.admin.repository.PresidentInfoRepository;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentClubRepository studentClubRepository;
    private final PresidentInfoRepository presidentInfoRepository;
    private final MemberInfoRepository memberInfoRepository;

    @Autowired
    public AdminService(UserRepository userRepository, StudentClubRepository studentClubRepository,
        PresidentInfoRepository presidentInfoRepository, MemberInfoRepository memberInfoRepository) {
        this.userRepository = userRepository;
        this.studentClubRepository = studentClubRepository;
        this.presidentInfoRepository = presidentInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
    }

    public User getPresident(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }
        return userRepository.findFirstByStudentClubAndRole(studentClub.get(), "PRESIDENT");
    }

    public PresidentInfo savePresident(Long clubId, PresidentDto presidentDto) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }
        PresidentInfo presidentInfo = new PresidentInfo();

        presidentInfo.setStudentNum(presidentInfo.getStudentNum());
        presidentInfo.setName(presidentInfo.getName());

        presidentInfo.setStudentClub(studentClub.get());
        return  presidentInfoRepository.save(presidentInfo);
    }
    @Transactional
    public PresidentInfo updatePresident(Long clubId, PresidentDto presidentDto) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }

        //회장 역할을 STU로 바꾸기 
        User user = userRepository.findFirstByStudentClubAndRole(studentClub.get(), "PRESIDENT");
        
        //MemberInfo에 추가하기
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStudentNum(user.getStudentNum());
        memberInfo.setName(user.getName());
        memberInfo.setStudentClub(user.getStudentClub());
        memberInfoRepository.save(memberInfo);



        //PresidentInfo 정보 새로 바꾸기
        PresidentInfo presidentInfo = presidentInfoRepository.findByStudentNum(user.getStudentNum());
        presidentInfo.setStudentNum(presidentDto.getStudentNum());
        presidentInfo.setName(presidentDto.getName());

        presidentInfo.setStudentClub(studentClub.get());

        presidentInfoRepository.save(presidentInfo);
        user.setRole("STU");
        userRepository.save(user);

        return presidentInfo;
    }

    public List<User> getMembers(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }
        return userRepository.findByStudentClubAndRole(studentClub.get(), "STU");
    }

    public MemberInfo saveMember(Long clubId, MemberDto memberDto) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new RuntimeException("학생회를 찾을 수 없습니다.");
        }
        MemberInfo memberInfo = new MemberInfo();
        convertToInfo(memberInfo, memberDto);
        memberInfo.setStudentClub(studentClub.get());
        return  memberInfoRepository.save(memberInfo);
    }

    public MemberInfo deleteMember(Long id) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findById(id);
        if (memberInfo.isEmpty()) {
            throw new RuntimeException("소속 부원을 찾을 수 없습니다.");
        }
        memberInfoRepository.deleteById(id);
        return memberInfo.get();
    }

    private void convertToInfo(MemberInfo memberInfo, MemberDto memberDto) {
        memberInfo.setStudentNum(memberDto.getStudentNum());
        memberInfo.setName(memberDto.getName());
    }


}
