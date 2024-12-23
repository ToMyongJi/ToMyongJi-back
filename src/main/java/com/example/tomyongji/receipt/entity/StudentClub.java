package com.example.tomyongji.receipt.entity;

import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.entity.President;
import com.example.tomyongji.auth.entity.User;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.Data;

@Entity
@Data
public class StudentClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentClubName;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "college_id")
    private College college;

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "president_id")
    private President president;


    public ClubDto toDto() {
        return ClubDto.builder()
                .studentClubId(this.id)
                .studentClubName(this.studentClubName)
                .build();
    }
}
