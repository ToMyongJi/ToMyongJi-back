package com.example.tomyongji.admin.repository;

import com.example.tomyongji.admin.entity.MemberInfo;
import com.example.tomyongji.receipt.entity.StudentClub;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {

    MemberInfo findByStudentNum(String studentNum);

    List<MemberInfo> findByStudentClub(StudentClub studentClub);

    Boolean existsByStudentNum(String studentNum);
}
