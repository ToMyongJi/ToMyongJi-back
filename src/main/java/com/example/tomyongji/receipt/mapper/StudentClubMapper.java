package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.admin.mapper.AdminMapper;
import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.OCRResultDto;
import com.example.tomyongji.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.receipt.dto.ReceiptDto;
import com.example.tomyongji.receipt.dto.ReceiptUpdateDto;
import com.example.tomyongji.receipt.entity.Receipt;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StudentClubMapper {

    StudentClubMapper INSTANCE = Mappers.getMapper(StudentClubMapper.class);

    // StudentClub Entity to StudentClub Dto
    @Mapping(source = "id", target = "studentClubId")
    ClubDto toClubDto(StudentClub studentClub);



}
