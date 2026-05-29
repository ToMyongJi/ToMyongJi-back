package com.example.tomyongji.domain.receipt.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Table(
//        name = "excel_column",
//        indexes = @Index(name = "idx_excel_column_student_club", columnList = "student_club_id")
//)
public class ExcelColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_club_id", unique = true, nullable = false)
    private StudentClub studentClub;

    @Column(name = "amount_type", nullable = false)
    private String amountType;

    @Column(name = "date_column_name")
    private String dateColumnName;

    @Column(name = "content_column_name")
    private String contentColumnName;

    @Column(name = "deposit_column_name")
    private String depositColumnName;

    @Column(name = "withdrawal_column_name")
    private String withdrawalColumnName;

    @Column(name = "amount_column_name")
    private String amountColumnName; // amountType이 SINGLE일 때 사용
}