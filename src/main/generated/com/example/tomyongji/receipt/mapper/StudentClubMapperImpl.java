package com.example.tomyongji.receipt.mapper;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.entity.StudentClub;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-27T18:21:31+0900",
    comments = "version: 1.5.0.Final, compiler: javac, environment: Java 17 (Oracle Corporation)"
)
@Component
public class StudentClubMapperImpl implements StudentClubMapper {

    @Override
    public ClubDto toClubDto(StudentClub studentClub) {
        if ( studentClub == null ) {
            return null;
        }

        ClubDto.ClubDtoBuilder clubDto = ClubDto.builder();

        if ( studentClub.getId() != null ) {
            clubDto.studentClubId( studentClub.getId() );
        }
        clubDto.studentClubName( studentClub.getStudentClubName() );

        return clubDto.build();
    }
}
