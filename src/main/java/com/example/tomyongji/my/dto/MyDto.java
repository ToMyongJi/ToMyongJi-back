package com.example.tomyongji.my.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyDto {

    private String name;
    private String studentNum;
    private String college;
    private long studentClubId;


}
