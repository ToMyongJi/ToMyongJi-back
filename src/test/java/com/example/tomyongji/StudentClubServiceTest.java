package com.example.tomyongji;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tomyongji.receipt.dto.ClubDto;
import com.example.tomyongji.receipt.entity.College;
import com.example.tomyongji.receipt.entity.StudentClub;
import com.example.tomyongji.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.example.tomyongji.receipt.service.StudentClubService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentClubServiceTest {

    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private StudentClubMapper studentClubMapper;
    @InjectMocks
    StudentClubService studentClubService;

    private College college;
    private StudentClub convergenceSoftware;
    private StudentClub digitalContentsDesign;
    private StudentClub business;
    private ClubDto convergenceSoftwareDto;
    private ClubDto digitalContentsDesignDto;
    private ClubDto businessDto;

    @BeforeEach
    void setUp() {
        college = College.builder()
            .id(1L)
            .collegeName("ICT 융합대학")
            .build();
        convergenceSoftware = StudentClub.builder()
            .id(1L)
            .studentClubName("융합소프트웨어학부 학생회")
            .Balance(1000)
            .college(college)
            .build();
        digitalContentsDesign = StudentClub.builder()
            .id(2L)
            .studentClubName("디지털콘텐츠디자인전공 학생회")
            .Balance(1000)
            .build();
        business = StudentClub.builder()
            .id(3L)
            .studentClubName("경영전공 학생회")
            .Balance(1000)
            .build();
        convergenceSoftwareDto = ClubDto.builder()
            .studentClubId(convergenceSoftware.getId())
            .studentClubName(convergenceSoftware.getStudentClubName())
            .build();
        digitalContentsDesignDto = ClubDto.builder()
            .studentClubId(digitalContentsDesign.getId())
            .studentClubName(digitalContentsDesign.getStudentClubName())
            .build();
        businessDto = ClubDto.builder()
            .studentClubId(business.getId())
            .studentClubName(business.getStudentClubName())
            .build();
    }

    @Test
    @DisplayName("모든 학생회 조회 성공")
    void getAllStudentClub_Success() {
        //Given
        List<StudentClub> studentClubList = List.of(convergenceSoftware, digitalContentsDesign, business);
        when(studentClubRepository.findAll()).thenReturn(studentClubList);
        when(studentClubMapper.toClubDto(convergenceSoftware)).thenReturn(convergenceSoftwareDto);
        when(studentClubMapper.toClubDto(digitalContentsDesign)).thenReturn(digitalContentsDesignDto);
        when(studentClubMapper.toClubDto(business)).thenReturn(businessDto);

        //When
        List<ClubDto> result = studentClubService.getAllStudentClub();
        //Then
        assertNotNull(result);
        assertEquals(result.size(), 3);
        assertEquals(result.get(0), convergenceSoftwareDto);
        assertEquals(result.get(1), digitalContentsDesignDto);
        assertEquals(result.get(2), businessDto);
        verify(studentClubRepository).findAll();
    }

    @Test
    @DisplayName("대학에 맞는 학생회 조회 성공")
    void getStudentClubById_Success() {
        //Given
        Long collegeId = college.getId();
        List<StudentClub> ictStudentClubList = List.of(convergenceSoftware, digitalContentsDesign);
        when(studentClubRepository.findAllByCollege_Id(collegeId)).thenReturn(ictStudentClubList);
        when(studentClubMapper.toClubDto(convergenceSoftware)).thenReturn(convergenceSoftwareDto);
        when(studentClubMapper.toClubDto(digitalContentsDesign)).thenReturn(digitalContentsDesignDto);
        //When
        List<ClubDto> result = studentClubService.getStudentClubById(collegeId);
        //Then
        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), convergenceSoftwareDto);
        assertEquals(result.get(1), digitalContentsDesignDto);
        verify(studentClubRepository).findAllByCollege_Id(collegeId);
    }

}
