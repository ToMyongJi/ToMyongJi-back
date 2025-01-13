package com.example.tomyongji;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.dto.CollegeDto;
import com.example.tomyongji.receipt.dto.CollegesDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.CollegeMapper;
import com.example.tomyongji.receipt.repository.CollegeRepository;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.receipt.service.CollegeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CollegeServiceTest {

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private CollegeMapper collegeMapper;

    @InjectMocks
    private CollegeService collegeService;

    @Test
    @DisplayName("모든 단과대별 학생회 조회 성공")
    void getAllCollegesAndClubs_Success() {
        //Given
        College ict = College.builder()
            .id(1L)
            .collegeName("ICT 융합대학")
            .build();
        CollegeDto ictDto = CollegeDto.builder()
            .collegeId(1L)
            .collegeName("ICT 융합대학")
            .build();
        College humanities = College.builder()
            .id(2L)
            .collegeName("인문대학")
            .build();
        CollegeDto humanitiesDto = CollegeDto.builder()
            .collegeId(2L)
            .collegeName("인문대학")
            .build();
        StudentClub convergenceSoftware = StudentClub.builder()
            .id(1L)
            .studentClubName("융합소프트웨어학부 학생회")
            .Balance(1000)
            .college(ict)
            .build();
        StudentClub digitalContentsDesign = StudentClub.builder()
            .id(2L)
            .studentClubName("디지털콘텐츠디자인전공 학생회")
            .Balance(1000)
            .college(ict)
            .build();
        StudentClub korean = StudentClub.builder()
            .id(3L)
            .studentClubName("국어국문학전공 학생회")
            .Balance(1000)
            .college(humanities)
            .build();
        ClubDto convergenceSoftwareDto = ClubDto.builder()
            .studentClubId(convergenceSoftware.getId())
            .studentClubName(convergenceSoftware.getStudentClubName())
            .build();
        ClubDto digitalContentsDesignDto = ClubDto.builder()
            .studentClubId(digitalContentsDesign.getId())
            .studentClubName(digitalContentsDesign.getStudentClubName())
            .build();
        ClubDto koreanDto = ClubDto.builder()
            .studentClubId(korean.getId())
            .studentClubName(korean.getStudentClubName())
            .build();
        List<StudentClub> ictList = List.of(convergenceSoftware, digitalContentsDesign);
        List<StudentClub> humanitiesList = List.of(korean);
        List<ClubDto> ictDtoList = List.of(convergenceSoftwareDto, digitalContentsDesignDto);
        List<ClubDto> humanitiesDtoList = List.of(koreanDto);
        when(studentClubRepository.findAllByCollege_Id(ict.getId())).thenReturn(ictList);
        when(studentClubRepository.findAllByCollege_Id(humanities.getId())).thenReturn(humanitiesList);
        when(collegeRepository.findAll()).thenReturn(List.of(ict, humanities));
        //When
        List<CollegesDto> result = collegeService.getAllCollegesAndClubs();
        //Then
        assertNotNull(result);
        assertEquals(result.get(0).getCollegeId(), ict.getId());
        assertEquals(result.get(0).getClubs(), ictDtoList);
        assertEquals(result.get(1).getCollegeId(), humanities.getId());
        assertEquals(result.get(1).getClubs(), humanitiesDtoList);
    }

}
