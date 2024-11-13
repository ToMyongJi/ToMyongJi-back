package com.example.tomyongji.admin.service;

import static com.example.tomyongji.validation.ErrorMsg.EXISTING_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.admin.entity.PresidentInfo;
import com.example.tomyongji.admin.repository.MemberInfoRepository;
import com.example.tomyongji.admin.repository.PresidentInfoRepository;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.MemberRequestDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.util.ArrayList;
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
    private final EmailVerificationRepository emailVerificationRepository;

    @Autowired
    public AdminService(UserRepository userRepository, StudentClubRepository studentClubRepository,
                        PresidentInfoRepository presidentInfoRepository, MemberInfoRepository memberInfoRepository,
                        EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.studentClubRepository = studentClubRepository;
        this.presidentInfoRepository = presidentInfoRepository;
        this.memberInfoRepository = memberInfoRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    public User getPresident(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        return userRepository.findFirstByStudentClubAndRole(studentClub.get(), "PRESIDENT");
    }

    @Transactional
    public void savePresident(Long clubId, PresidentDto presidentDto) {
        if (presidentInfoRepository.existsByStudentNum(presidentDto.getStudentNum())) {
            throw new CustomException(EXISTING_USER, 400);  // 중복 학번 예외 처리
        }

        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        PresidentInfo presidentInfo = new PresidentInfo();
        // DTO에서 값을 가져와 설정
        presidentInfo.setStudentNum(presidentDto.getStudentNum());
        presidentInfo.setName(presidentDto.getName());

        presidentInfoRepository.save(presidentInfo);

        // StudentClub 객체를 가져와서 PresidentInfo와의 관계 설정
        StudentClub studentClubEntity = studentClub.get();
        presidentInfo.setStudentClub(studentClubEntity);
        studentClubEntity.setPresidentInfo(presidentInfo);

        // 양방향 관계가 설정된 상태에서 StudentClub과 PresidentInfo를 저장
        studentClubRepository.save(studentClubEntity);  // StudentClub을 먼저 저장

    }

    @Transactional
    public void updatePresident(Long clubId, PresidentDto presidentDto) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
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

        StudentClub studentClubEntity = studentClub.get();
        presidentInfo.setStudentClub(studentClub.get());
        studentClubEntity.setPresidentInfo(presidentInfo);

        studentClubRepository.save(studentClubEntity);
        presidentInfoRepository.save(presidentInfo);

        user.setRole("STU");
        userRepository.save(user);

    }

    public List<MemberDto> getMembers(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        List<User> users = userRepository.findByStudentClubAndRole(studentClub.get(), "STU");
        return convertToMemberDtoList(users);
    }

    public void saveMember(Long clubId, MemberRequestDto memberDto) {
        if (memberInfoRepository.existsByStudentNum(memberDto.getStudentNum())) {
            throw new CustomException(EXISTING_USER, 400);  // 중복 학번 예외 처리
        }
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        MemberInfo memberInfo = new MemberInfo();
        convertToInfo(memberInfo, memberDto);
        memberInfo.setStudentClub(studentClub.get());
        memberInfoRepository.save(memberInfo);
    }

    public MemberDto deleteMember(Long id) {
        Optional<MemberInfo> memberInfo = memberInfoRepository.findById(id);
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
        memberInfoRepository.deleteById(id);
        return memberDto;
    }

    private void convertToInfo(MemberInfo memberInfo, MemberRequestDto memberDto) {
        memberInfo.setStudentNum(memberDto.getStudentNum());
        memberInfo.setName(memberDto.getName());
    }

    private MemberDto convertToMemberDto(User user) {
        MemberDto memberDto = new MemberDto();
        memberDto.setStudentNum(user.getStudentNum());
        memberDto.setName(user.getName());
        return memberDto;
    }

    private MemberDto convertToMemberDto(MemberInfo memberInfo) {
        MemberDto memberDto = new MemberDto();
        memberDto.setStudentNum(memberInfo.getStudentNum());
        memberDto.setName(memberInfo.getName());
        return memberDto;
    }

    private List<MemberDto> convertToMemberDtoList(List<User> users) {
        List<MemberDto> members = new ArrayList<>();
        for(User user : users) {
            MemberDto memberDto = convertToMemberDto(user);
            members.add(memberDto);
        }
        return members;
    }


}
