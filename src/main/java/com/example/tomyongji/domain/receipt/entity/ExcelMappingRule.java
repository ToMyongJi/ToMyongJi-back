package com.example.tomyongji.domain.receipt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_mapping_rule")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelMappingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "studentclub_id", unique = true)
    private StudentClub studentClub;

    @Column(name = "date_column", length = 2)
    private String dateColumn;

    @Column(name = "content_column", length = 2)
    private String contentColumn;

    @Column(name = "amount_type", length = 10)
    private String amountType; // "split" or "single"

    @Column(name = "deposit_column", length = 2)
    private String depositColumn;

    @Column(name = "withdrawal_column", length = 2)
    private String withdrawalColumn;

    @Column(name = "amount_column", length = 2)
    private String amountColumn;

    @Column(name = "data_start_row")
    private Integer dataStartRow;

    @Column(name = "date_format", length = 30)
    private String dateFormat;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
