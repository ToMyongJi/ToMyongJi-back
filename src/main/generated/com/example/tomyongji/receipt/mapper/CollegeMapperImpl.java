package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.entity.College;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-30T18:11:46+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class CollegeMapperImpl implements CollegeMapper {

    @Override
    public CollegeDto toCollegeDto(College college) {
        if ( college == null ) {
            return null;
        }

        CollegeDto collegeDto = new CollegeDto();

        if ( college.getId() != null ) {
            collegeDto.setCollegeId( college.getId() );
        }
        collegeDto.setCollegeName( college.getCollegeName() );

        return collegeDto;
    }
}
