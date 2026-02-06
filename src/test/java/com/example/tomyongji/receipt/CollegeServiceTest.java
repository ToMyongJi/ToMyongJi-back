package com.example.tomyongji.receipt;

import com.example.tomyongji.domain.receipt.dto.ClubDto;
import com.example.tomyongji.domain.receipt.dto.CollegeDto;
import com.example.tomyongji.domain.receipt.dto.CollegesDto;
import com.example.tomyongji.domain.receipt.entity.College;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.CollegeMapper;
import com.example.tomyongji.domain.receipt.mapper.StudentClubMapper;
import com.example.tomyongji.domain.receipt.repository.CollegeRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.service.CollegeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CollegeServiceTest {

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private StudentClubRepository studentClubRepository;

    @Mock
    private CollegeMapper collegeMapper;

    @Mock
    private StudentClubMapper studentClubMapper;

    @InjectMocks
    private CollegeService collegeService;

    private College ictCollege;
    private College humanitiesCollege;
    private StudentClub convergenceSoftwareClub;
    private StudentClub digitalContentsDesignClub;
    private StudentClub koreanClub;

    @BeforeEach
    void setUp() {
        ictCollege = createCollege(1L, "ICT 융합대학");
        humanitiesCollege = createCollege(2L, "인문대학");

        convergenceSoftwareClub = createStudentClub(1L, "융합소프트웨어학부 학생회", 1000, ictCollege);
        digitalContentsDesignClub = createStudentClub(2L, "디지털콘텐츠디자인전공 학생회", 1000, ictCollege);
        koreanClub = createStudentClub(3L, "국어국문학전공 학생회", 1000, humanitiesCollege);
    }

    private College createCollege(Long id, String name) {
        return College.builder()
                .id(id)
                .collegeName(name)
                .build();
    }

    private StudentClub createStudentClub(Long id, String name, Integer balance, College college) {
        return StudentClub.builder()
                .id(id)
                .studentClubName(name)
                .Balance(balance)
                .college(college)
                .build();
    }

    @Nested
    @DisplayName("getAllCollegesAndClubs 메서드는")
    class Describe_getAllCollegesAndClubs {

        @Nested
        @DisplayName("단과대와 학생회가 존재하면")
        class Context_with_colleges_and_clubs {

            @Test
            @DisplayName("모든 단과대와 소속 학생회 목록을 반환한다")
            void it_returns_all_colleges_with_clubs() {
                // given
                List<College> colleges = List.of(ictCollege, humanitiesCollege);
                List<StudentClub> ictClubs = List.of(convergenceSoftwareClub, digitalContentsDesignClub);
                List<StudentClub> humanitiesClubs = List.of(koreanClub);

                ClubDto convergenceSoftwareClubDto = new ClubDto(1L, "융합소프트웨어학부 학생회", false);
                ClubDto digitalContentsDesignClubDto = new ClubDto(2L, "디지털콘텐츠디자인전공 학생회", false);
                ClubDto koreanClubDto = new ClubDto(3L, "국어국문학전공 학생회", false);

                given(collegeRepository.findAll()).willReturn(colleges);
                given(studentClubRepository.findAllByCollege_Id(ictCollege.getId())).willReturn(ictClubs);
                given(studentClubRepository.findAllByCollege_Id(humanitiesCollege.getId())).willReturn(humanitiesClubs);
                given(studentClubMapper.toClubDto(convergenceSoftwareClub)).willReturn(convergenceSoftwareClubDto);
                given(studentClubMapper.toClubDto(digitalContentsDesignClub)).willReturn(digitalContentsDesignClubDto);
                given(studentClubMapper.toClubDto(koreanClub)).willReturn(koreanClubDto);

                // when
                List<CollegesDto> result = collegeService.getAllCollegesAndClubs();

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);

                // ICT 융합대학 검증
                assertThat(result.get(0).getCollegeId()).isEqualTo(ictCollege.getId());
                assertThat(result.get(0).getCollegeName()).isEqualTo(ictCollege.getCollegeName());
                assertThat(result.get(0).getClubs()).hasSize(2);

                // 인문대학 검증
                assertThat(result.get(1).getCollegeId()).isEqualTo(humanitiesCollege.getId());
                assertThat(result.get(1).getCollegeName()).isEqualTo(humanitiesCollege.getCollegeName());
                assertThat(result.get(1).getClubs()).hasSize(1);

                then(collegeRepository).should().findAll();
                then(studentClubRepository).should().findAllByCollege_Id(ictCollege.getId());
                then(studentClubRepository).should().findAllByCollege_Id(humanitiesCollege.getId());
            }
        }

        @Nested
        @DisplayName("단과대는 있지만 학생회가 없으면")
        class Context_with_colleges_but_no_clubs {

            @Test
            @DisplayName("빈 학생회 목록과 함께 단과대 정보를 반환한다")
            void it_returns_colleges_with_empty_clubs() {
                // given
                List<College> colleges = List.of(ictCollege);
                List<StudentClub> emptyClubs = Collections.emptyList();

                given(collegeRepository.findAll()).willReturn(colleges);
                given(studentClubRepository.findAllByCollege_Id(ictCollege.getId())).willReturn(emptyClubs);

                // when
                List<CollegesDto> result = collegeService.getAllCollegesAndClubs();

                // then
                assertThat(result).isNotNull()
                        .hasSize(1);

                assertThat(result.get(0).getCollegeId()).isEqualTo(ictCollege.getId());
                assertThat(result.get(0).getCollegeName()).isEqualTo(ictCollege.getCollegeName());
                assertThat(result.get(0).getClubs()).isEmpty();

                then(collegeRepository).should().findAll();
                then(studentClubRepository).should().findAllByCollege_Id(ictCollege.getId());
            }
        }

        @Nested
        @DisplayName("등록된 단과대가 없으면")
        class Context_with_no_colleges {

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // given
                List<College> emptyColleges = Collections.emptyList();

                given(collegeRepository.findAll()).willReturn(emptyColleges);

                // when
                List<CollegesDto> result = collegeService.getAllCollegesAndClubs();

                // then
                assertThat(result).isNotNull()
                        .isEmpty();

                then(collegeRepository).should().findAll();
            }
        }

        @Nested
        @DisplayName("단일 단과대에 여러 학생회가 존재하면")
        class Context_with_single_college_multiple_clubs {

            @Test
            @DisplayName("해당 단과대의 모든 학생회를 올바르게 매핑한다")
            void it_maps_all_clubs_correctly() {
                // given
                List<College> colleges = List.of(ictCollege);
                List<StudentClub> ictClubs = List.of(
                        convergenceSoftwareClub,
                        digitalContentsDesignClub
                );

                ClubDto convergenceSoftwareClubDto = new ClubDto(1L, "융합소프트웨어학부 학생회", false);
                ClubDto digitalContentsDesignClubDto = new ClubDto(2L, "디지털콘텐츠디자인전공 학생회", false);

                given(collegeRepository.findAll()).willReturn(colleges);
                given(studentClubRepository.findAllByCollege_Id(ictCollege.getId())).willReturn(ictClubs);
                given(studentClubMapper.toClubDto(convergenceSoftwareClub)).willReturn(convergenceSoftwareClubDto);
                given(studentClubMapper.toClubDto(digitalContentsDesignClub)).willReturn(digitalContentsDesignClubDto);

                // when
                List<CollegesDto> result = collegeService.getAllCollegesAndClubs();

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getClubs()).hasSize(2);

                then(collegeRepository).should().findAll();
                then(studentClubRepository).should().findAllByCollege_Id(ictCollege.getId());
            }
        }

        @Nested
        @DisplayName("여러 단과대가 각각 다른 수의 학생회를 가지면")
        class Context_with_multiple_colleges_different_club_counts {

            @Test
            @DisplayName("각 단과대별로 올바른 학생회 수를 반환한다")
            void it_returns_correct_club_counts_per_college() {
                // given
                College engineeringCollege = createCollege(3L, "공과대학");
                List<College> colleges = List.of(ictCollege, humanitiesCollege, engineeringCollege);

                List<StudentClub> ictClubs = List.of(convergenceSoftwareClub, digitalContentsDesignClub);
                List<StudentClub> humanitiesClubs = List.of(koreanClub);
                List<StudentClub> engineeringClubs = Collections.emptyList();

                ClubDto convergenceSoftwareClubDto = new ClubDto(1L, "융합소프트웨어학부 학생회", false);
                ClubDto digitalContentsDesignClubDto = new ClubDto(2L, "디지털콘텐츠디자인전공 학생회", false);
                ClubDto koreanClubDto = new ClubDto(3L, "국어국문학전공 학생회", false);

                given(collegeRepository.findAll()).willReturn(colleges);
                given(studentClubRepository.findAllByCollege_Id(ictCollege.getId())).willReturn(ictClubs);
                given(studentClubRepository.findAllByCollege_Id(humanitiesCollege.getId())).willReturn(humanitiesClubs);
                given(studentClubRepository.findAllByCollege_Id(engineeringCollege.getId())).willReturn(engineeringClubs);
                given(studentClubMapper.toClubDto(convergenceSoftwareClub)).willReturn(convergenceSoftwareClubDto);
                given(studentClubMapper.toClubDto(digitalContentsDesignClub)).willReturn(digitalContentsDesignClubDto);
                given(studentClubMapper.toClubDto(koreanClub)).willReturn(koreanClubDto);

                // when
                List<CollegesDto> result = collegeService.getAllCollegesAndClubs();

                // then
                assertThat(result).hasSize(3);
                assertThat(result.get(0).getClubs()).hasSize(2);
                assertThat(result.get(1).getClubs()).hasSize(1);
                assertThat(result.get(2).getClubs()).isEmpty();

                then(collegeRepository).should().findAll();
                then(studentClubRepository).should().findAllByCollege_Id(ictCollege.getId());
                then(studentClubRepository).should().findAllByCollege_Id(humanitiesCollege.getId());
                then(studentClubRepository).should().findAllByCollege_Id(engineeringCollege.getId());
            }
        }
    }
}