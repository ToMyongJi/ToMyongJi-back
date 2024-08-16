package com.example.tomyongji.admin.entity;

import com.example.tomyongji.receipt.entity.StudentClub;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class MemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentNum;
    private String name;

    @ManyToOne
    @JsonBackReference
    private StudentClub studentClub;
}
