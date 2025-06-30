package com.example.tomyongji.receipt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class BreakDown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String issueNumber;
    @Temporal(TemporalType.DATE)
    private Date issueDate;

    @ManyToOne
    private StudentClub studentClub;
}