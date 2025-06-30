package com.example.tomyongji.receipt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class TempReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date date;
    private int amount;
    private String contents;

    @ManyToOne
    private BreakDown breakDown;

    public TempReceipt(Date date, int amount, String contents ) {
        this.date = date;
        this.amount = amount;
        this.contents = contents;
    }

    public TempReceipt() {

    }
}
