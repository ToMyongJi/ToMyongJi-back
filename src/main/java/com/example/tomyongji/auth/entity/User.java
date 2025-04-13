package com.example.tomyongji.auth.entity;

import com.example.tomyongji.admin.dto.MemberDto;
import com.example.tomyongji.admin.dto.PresidentDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    @NotBlank(message="사용자 아이디는 필수 입력값입니다")
    private String userId;
    @NotBlank(message="이름은 필수 입력값입니다")
    private String name;
    @NotBlank(message="학번은 필수 입력값입니다")
    private String studentNum;
    @NotBlank(message="학부는 필수 입력값입니다")
    private String collegeName;
    @NotBlank(message="이메일은 필수 입력값입니다")
    private String email;
    @NotBlank(message="비밀번호는 필수 입력값입니다")
    private String password;
    @NotBlank(message="계정 타입은 필수 입력값입니다")
    private String role;

    @ManyToOne
    @JsonBackReference
    private StudentClub studentClub;
}
