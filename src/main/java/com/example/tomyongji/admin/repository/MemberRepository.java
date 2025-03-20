package com.example.tomyongji.admin.repository;

import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.receipt.entity.StudentClub;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByStudentNum(String studentNum);

    List<Member> findByStudentClub(StudentClub studentClub);

    Boolean existsByStudentNum(String studentNum);


}
