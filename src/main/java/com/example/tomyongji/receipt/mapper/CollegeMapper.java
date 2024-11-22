package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CollegeMapper {

    CollegeMapper INSTANCE = Mappers.getMapper(CollegeMapper.class);

    // StudentClub Entity to StudentClub Dto
    @Mapping(source = "id", target = "collegeId")
    CollegeDto toCollegeDto(College college);


}
