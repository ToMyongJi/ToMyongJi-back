package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.BreakDownDto;
import com.example.tomyongji.receipt.entity.BreakDown;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BreakDownMapper {
    BreakDownMapper INSTANCE = Mappers.getMapper(BreakDownMapper.class);

    @Mapping(source = "studentClub.studentClubName", target = "studentClubName")
    BreakDownDto toBreakDownDto(BreakDown breakDown);
}
