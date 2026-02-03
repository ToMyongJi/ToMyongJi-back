package com.example.tomyongji.domain.receipt.mapper;

import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
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
