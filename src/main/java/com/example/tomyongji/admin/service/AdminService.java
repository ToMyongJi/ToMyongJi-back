package com.example.tomyongji.admin.service;

import static com.example.tomyongji.validation.ErrorMsg.EXISTING_USER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_MEMBER;
import static com.example.tomyongji.validation.ErrorMsg.NOT_FOUND_STUDENT_CLUB;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.admin.repository.PresidentRepository;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.auth.repository.EmailVerificationRepository;
import com.example.tomyongji.auth.repository.UserRepository;
import com.example.tomyongji.my.dto.AdminSaveMemberDto;
import com.example.tomyongji.my.dto.SaveMemberDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.validation.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentClubRepository studentClubRepository;
    private final PresidentRepository presidentRepository;
    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final AdminMapper adminMapper;

    @Autowired
    public AdminService(UserRepository userRepository, StudentClubRepository studentClubRepository,
                        PresidentRepository presidentRepository, MemberRepository memberRepository,
                        EmailVerificationRepository emailVerificationRepository,
        AdminMapper adminMapper) {
        this.userRepository = userRepository;
        this.studentClubRepository = studentClubRepository;
        this.presidentRepository = presidentRepository;
        this.memberRepository = memberRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.adminMapper = adminMapper;
    }

    public PresidentDto getPresident(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        President president = studentClub.get().getPresident();
        PresidentDto presidentDto = adminMapper.toPresidentDto(president);
        presidentDto.setClubId(clubId);
        return presidentDto;
    }

    @Transactional
    public PresidentDto savePresident(PresidentDto presidentDto) {
        if (presidentRepository.existsByStudentNum(presidentDto.getStudentNum())) {
            throw new CustomException(EXISTING_USER, 400);  // 중복 학번 예외 처리
        }
        President president = adminMapper.toPresidentEntity(presidentDto);
        President returnPresident = presidentRepository.save(president);

        Optional<StudentClub> studentClub = studentClubRepository.findById(presidentDto.getClubId());
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }

        StudentClub studentClubEntity = studentClub.get();
        studentClubEntity.setPresident(president);
        studentClubRepository.save(studentClubEntity);

        return adminMapper.toPresidentDto(returnPresident);
    }

    public PresidentDto updatePresident(PresidentDto presidentDto) {
        StudentClub studentClub = studentClubRepository.findById(presidentDto.getClubId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_STUDENT_CLUB, 400));

        User user = userRepository.findFirstByStudentClubAndRole(studentClub, "PRESIDENT");
        user.setRole("STU");
        userRepository.save(user);

        //MemberInfo에 추가하기
        Member member =adminMapper.toMemberEntity(user);
        memberRepository.save(member);

        //PresidentInfo 정보 새로 바꾸기
        President president = presidentRepository.findByStudentNum(user.getStudentNum()); //기존 회장 불러오기
        president.setStudentNum(presidentDto.getStudentNum()); //학번 고치기
        president.setName(presidentDto.getName()); //이름 고치기

        studentClub.setPresident(president);

        studentClubRepository.save(studentClub);
        President returnPresident = presidentRepository.save(president);
        return adminMapper.toPresidentDto(returnPresident);
    }

    public List<MemberDto> getMembers(Long clubId) {
        Optional<StudentClub> studentClub = studentClubRepository.findById(clubId);
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        List<Member> members = memberRepository.findByStudentClub(studentClub.get());
        return members.stream().map(adminMapper::toMemberDto).collect(Collectors.toList());
    }

    public MemberDto saveMember(AdminSaveMemberDto memberDto) {
        if (memberRepository.existsByStudentNum(memberDto.getStudentNum())) {
            throw new CustomException(EXISTING_USER, 400);  // 중복 학번 예외 처리
        }
        Optional<StudentClub> studentClub = studentClubRepository.findById(memberDto.getClubId());
        if (studentClub.isEmpty()) {
            throw new CustomException(NOT_FOUND_STUDENT_CLUB, 400);
        }
        //멤버 생성 후
        //받은 dto의 정보를 멤버에 삽입
        //멤버의 학생회 정보를 따로 삽입
        //해당 멤버를 레포지터리에 저장
        Member member = adminMapper.toMemberEntity(memberDto);
        member.setStudentClub(studentClub.get());
        Member returnMember = memberRepository.save(member);
        return adminMapper.toMemberDto(returnMember);
    }

    public MemberDto deleteMember(Long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            throw new CustomException(NOT_FOUND_MEMBER, 400);
        }
        MemberDto memberDto = adminMapper.toMemberDto(member.get()); //삭제된 멤버정보 반환을 위한 저장
        //멤버 등록을 해도 유저가 없을 수 있음
        Optional<User> user = Optional.ofNullable(
            userRepository.findByStudentNum(member.get().getStudentNum()));

        //유저가 있다면 유저의 메일과 유저를 삭제
        if (user.isPresent()) {
            emailVerificationRepository.deleteByEmail(user.get().getEmail());
            userRepository.delete(user.get());
        }
        //등록된 멤버 정보도 삭제
        memberRepository.deleteById(memberId);
        return memberDto;
    }
}
