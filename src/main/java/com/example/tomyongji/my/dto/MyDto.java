package com.example.tomyongji.my.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyDto {

    private String name;
    private String studentNum;
    private String college;
    private long studentClubId;


}
