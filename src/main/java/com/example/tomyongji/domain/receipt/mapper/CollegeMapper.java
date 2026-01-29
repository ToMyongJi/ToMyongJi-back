package com.example.tomyongji.domain.receipt.mapper;

import com.example.tomyongji.domain.receipt.dto.CollegeDto;
import com.example.tomyongji.domain.receipt.entity.College;
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
