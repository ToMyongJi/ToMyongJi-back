package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.entity.College;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-13T17:07:56+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class CollegeMapperImpl implements CollegeMapper {

    @Override
    public CollegeDto toCollegeDto(College college) {
        if ( college == null ) {
            return null;
        }

        CollegeDto.CollegeDtoBuilder collegeDto = CollegeDto.builder();

        if ( college.getId() != null ) {
            collegeDto.collegeId( college.getId() );
        }
        collegeDto.collegeName( college.getCollegeName() );

        return collegeDto.build();
    }
}
