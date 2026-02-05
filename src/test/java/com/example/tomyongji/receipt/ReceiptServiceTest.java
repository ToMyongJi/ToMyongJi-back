package com.example.tomyongji.receipt;

import static com.example.tomyongji.global.error.ErrorMsg.DUPLICATED_FLOW;
import static com.example.tomyongji.global.error.ErrorMsg.EMPTY_CONTENT;
import static com.example.tomyongji.global.error.ErrorMsg.INVALID_KEYWORD;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_RECEIPT;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_STUDENT_CLUB;
import static com.example.tomyongji.global.error.ErrorMsg.NOT_FOUND_USER;
import static com.example.tomyongji.global.error.ErrorMsg.NO_AUTHORIZATION_BELONGING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.tomyongji.domain.auth.entity.User;
import com.example.tomyongji.domain.auth.repository.UserRepository;
import com.example.tomyongji.domain.receipt.dto.PagingReceiptDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptByStudentClubDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptCreateDto;
import com.example.tomyongji.domain.receipt.dto.ReceiptDto;
import com.example.tomyongji.domain.receipt.entity.Receipt;
import com.example.tomyongji.domain.receipt.entity.StudentClub;
import com.example.tomyongji.domain.receipt.mapper.ReceiptMapper;
import com.example.tomyongji.domain.receipt.repository.ReceiptRepository;
import com.example.tomyongji.domain.receipt.repository.StudentClubRepository;
import com.example.tomyongji.domain.receipt.service.ReceiptService;
import com.example.tomyongji.global.error.CustomException;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private StudentClubRepository studentClubRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReceiptMapper receiptMapper;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @InjectMocks
    private ReceiptService receiptService;

    private StudentClub studentClub;
    private StudentClub anotherStudentClub;
    private User user;
    private User anotherUser;
    private Receipt receipt;
    private UserDetails currentUser;
    private UserDetails anotherCurrentUser;

    @BeforeEach
    void setUp() {
        studentClub = createStudentClub(30L, "스마트시스템공과대학 학생회");
        anotherStudentClub = createStudentClub(35L, "아너칼리지(자연)");

        user = createUser(
                1L, "testUser", "test name", "60000000",
                studentClub, "스마트시스템공과대학",
                "test@example.com", "password123", "PRESIDENT"
        );

        anotherUser = createUser(
                2L, "anotherUser", "test name2", "60000001",
                anotherStudentClub, "아너칼리지",
                "test2@example.com", "password123", "PRESIDENT"
        );

        receipt = createReceipt(1L, "영수증 테스트", 1000, 0, studentClub);

        currentUser = createUserDetails("testUser", "password123");
        anotherCurrentUser = createUserDetails("anotherUser", "password123");
    }

    private StudentClub createStudentClub(Long id, String name) {
        return StudentClub.builder()
                .id(id)
                .studentClubName(name)
                .build();
    }

    private User createUser(Long id, String userId, String name, String studentNum,
                            StudentClub studentClub, String collegeName, String email,
                            String password, String role) {
        return User.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .studentNum(studentNum)
                .collegeName(collegeName)
                .email(email)
                .password(password)
                .role(role)
                .studentClub(studentClub)
                .build();
    }

    private Receipt createReceipt(Long id, String content, int deposit, int withdrawal, StudentClub studentClub) {
        return Receipt.builder()
                .id(id)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .studentClub(studentClub)
                .build();
    }

    private ReceiptDto createReceiptDto(Long id, String content, int deposit, int withdrawal) {
        return ReceiptDto.builder()
                .receiptId(id)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .build();
    }

    private ReceiptCreateDto createReceiptCreateDto(String userId, String content, int deposit, int withdrawal) {
        return ReceiptCreateDto.builder()
                .userId(userId)
                .content(content)
                .deposit(deposit)
                .withdrawal(withdrawal)
                .build();
    }

    private UserDetails createUserDetails(String username, String password) {
        return new org.springframework.security.core.userdetails.User(
                username,
                password,
                Collections.emptyList()
        );
    }


    @Nested
    @DisplayName("createReceipt 메서드는")
    class Describe_createReceipt {

        @Nested
        @DisplayName("유효한 영수증 정보가 주어지면")
        class Context_with_valid_receipt {

            @Test
            @DisplayName("영수증을 성공적으로 생성한다")
            void it_creates_successfully() {
                // given
                ReceiptCreateDto receiptCreateDto = createReceiptCreateDto("testUser", "영수증 테스트", 1000, 0);
                ReceiptDto receiptDto = createReceiptDto(1L, "영수증 테스트", 1000, 0);
                Long clubId = studentClub.getId();
                String cachePattern = "receiptList::" + clubId + ":*";

                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptMapper.toReceiptEntity(receiptCreateDto)).willReturn(receipt);
                given(redisTemplate.keys(cachePattern)).willReturn(java.util.Set.of("receiptList::" + clubId + ":p0_s10_null_null"));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);

                // when
                ReceiptDto result = receiptService.createReceipt(receiptCreateDto, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo("영수증 테스트");
                assertThat(result.getDeposit()).isEqualTo(1000);

                then(receiptRepository).should().save(receipt);
                then(studentClubRepository).should().save(studentClub);
                then(redisTemplate).should().keys(cachePattern);
                then(redisTemplate).should().delete(any(java.util.Set.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // given
                ReceiptCreateDto receiptCreateDto = createReceiptCreateDto("wrongUser", "영수증 테스트", 1000, 0);
                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.createReceipt(receiptCreateDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);

                then(userRepository).should().findByUserId(receiptCreateDto.getUserId());
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                ReceiptCreateDto receiptCreateDto = createReceiptCreateDto("testUser", "영수증 테스트", 1000, 0);
                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherUser.getUserId())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> receiptService.createReceipt(receiptCreateDto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);
            }
        }

        @Nested
        @DisplayName("입금과 출금이 모두 입력되면")
        class Context_with_both_deposit_and_withdrawal {

            @Test
            @DisplayName("DUPLICATED_FLOW 예외를 던진다")
            void it_throws_duplicated_flow_exception() {
                // given
                ReceiptCreateDto receiptCreateDto = createReceiptCreateDto("testUser", "영수증 테스트", 1000, 1000);
                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.createReceipt(receiptCreateDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", DUPLICATED_FLOW);

                then(userRepository).should(times(2)).findByUserId(receiptCreateDto.getUserId());
            }
        }

        @Nested
        @DisplayName("입금과 출금이 모두 0이면")
        class Context_with_both_zero {

            @Test
            @DisplayName("DUPLICATED_FLOW 예외를 던진다")
            void it_throws_duplicated_flow_exception() {
                // given
                ReceiptCreateDto receiptCreateDto = createReceiptCreateDto("testUser", "영수증 테스트", 0, 0);
                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.createReceipt(receiptCreateDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", DUPLICATED_FLOW);

                then(userRepository).should(times(2)).findByUserId(receiptCreateDto.getUserId());
            }
        }

        @Nested
        @DisplayName("영수증 내용이 비어있으면")
        class Context_with_empty_content {

            @Test
            @DisplayName("EMPTY_CONTENT 예외를 던진다")
            void it_throws_empty_content_exception() {
                // given
                ReceiptCreateDto receiptCreateDto = ReceiptCreateDto.builder()
                        .userId("testUser")
                        .deposit(1000)
                        .build();
                given(userRepository.findByUserId(receiptCreateDto.getUserId())).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.createReceipt(receiptCreateDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", EMPTY_CONTENT);

                then(userRepository).should(times(2)).findByUserId(receiptCreateDto.getUserId());
            }
        }
    }

    @Nested
    @DisplayName("getAllReceipts 메서드는")
    class Describe_getAllReceipts {

        @Nested
        @DisplayName("영수증이 존재하면")
        class Context_with_receipts {

            @Test
            @DisplayName("모든 영수증을 반환한다")
            void it_returns_all_receipts() {
                // given
                Receipt receipt1 = createReceipt(2L, "영수증 테스트1", 2000, 0, studentClub);
                Receipt receipt2 = createReceipt(3L, "영수증 테스트2", 3000, 0, studentClub);
                List<Receipt> receiptList = List.of(receipt1, receipt2);

                ReceiptDto receiptDto1 = createReceiptDto(2L, "영수증 테스트1", 2000, 0);
                ReceiptDto receiptDto2 = createReceiptDto(3L, "영수증 테스트2", 3000, 0);

                given(receiptRepository.findAll()).willReturn(receiptList);
                given(receiptMapper.toReceiptDto(receipt1)).willReturn(receiptDto1);
                given(receiptMapper.toReceiptDto(receipt2)).willReturn(receiptDto2);

                // when
                List<ReceiptDto> result = receiptService.getAllReceipts();

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);
                assertThat(result.get(0)).isEqualTo(receiptDto1);
                assertThat(result.get(1)).isEqualTo(receiptDto2);

                then(receiptRepository).should().findAll();
            }
        }
    }

    @Nested
    @DisplayName("getReceiptsByClub 메서드는")
    class Describe_getReceiptsByClub {

        @Nested
        @DisplayName("유효한 유저 ID가 주어지면")
        class Context_with_valid_user_id {

            @Test
            @DisplayName("해당 학생회의 모든 영수증을 반환한다")
            void it_returns_all_receipts_of_club() {
                // given
                Long id = user.getId();
                Receipt receipt1 = createReceipt(2L, "영수증 테스트1", 2000, 0, studentClub);
                Receipt receipt2 = createReceipt(3L, "영수증 테스트2", 3000, 0, studentClub);
                List<Receipt> receiptList = List.of(receipt1, receipt2);

                ReceiptDto receiptDto1 = createReceiptDto(2L, "영수증 테스트1", 2000, 0);
                ReceiptDto receiptDto2 = createReceiptDto(3L, "영수증 테스트2", 3000, 0);

                given(userRepository.findById(id)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptRepository.findAllByStudentClubOrderByIdDesc(any(StudentClub.class))).willReturn(receiptList);
                given(receiptMapper.toReceiptDto(any(Receipt.class))).willAnswer(invocation -> {
                    Receipt arg = invocation.getArgument(0);
                    if (arg.getId() == 2L) return receiptDto1;
                    if (arg.getId() == 3L) return receiptDto2;
                    return null;
                });

                // when
                ReceiptByStudentClubDto result = receiptService.getReceiptsByClub(id, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getReceiptList().get(0)).isEqualTo(receiptDto1);
                assertThat(result.getReceiptList().get(1)).isEqualTo(receiptDto2);

                then(receiptRepository).should().findAllByStudentClubOrderByIdDesc(studentClub);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // given
                Long wrongId = 999L;
                given(userRepository.findById(wrongId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.getReceiptsByClub(wrongId, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);

                then(userRepository).should().findById(wrongId);
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                Long id = user.getId();
                given(userRepository.findById(id)).willReturn(Optional.of(user));
                given(userRepository.findByUserId(anotherUser.getUserId())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> receiptService.getReceiptsByClub(id, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(userRepository).should().findById(id);
            }
        }
    }

    @Nested
    @DisplayName("getReceiptsByClubForStudent 메서드는")
    class Describe_getReceiptsByClubForStudent {

        @Nested
        @DisplayName("유효한 학생회 ID가 주어지면")
        class Context_with_valid_club_id {

            @Test
            @DisplayName("해당 학생회의 모든 영수증을 반환한다")
            void it_returns_all_receipts() {
                // given
                Long clubId = studentClub.getId();
                Receipt receipt1 = createReceipt(2L, "영수증 테스트1", 2000, 0, studentClub);
                Receipt receipt2 = createReceipt(3L, "영수증 테스트2", 3000, 0, studentClub);
                List<Receipt> receiptList = List.of(receipt1, receipt2);

                ReceiptDto receiptDto1 = createReceiptDto(2L, "영수증 테스트1", 2000, 0);
                ReceiptDto receiptDto2 = createReceiptDto(3L, "영수증 테스트2", 3000, 0);

                given(studentClubRepository.findById(clubId)).willReturn(Optional.of(studentClub));
                given(receiptRepository.findAllByStudentClubOrderByIdDesc(studentClub)).willReturn(receiptList);
                given(receiptMapper.toReceiptDto(receipt1)).willReturn(receiptDto1);
                given(receiptMapper.toReceiptDto(receipt2)).willReturn(receiptDto2);

                // when
                List<ReceiptDto> result = receiptService.getReceiptsByClubForStudent(clubId);

                // then
                assertThat(result).isNotNull()
                        .hasSize(2);
                assertThat(result.get(0)).isEqualTo(receiptDto1);
                assertThat(result.get(1)).isEqualTo(receiptDto2);

                then(studentClubRepository).should().findById(clubId);
                then(receiptRepository).should().findAllByStudentClubOrderByIdDesc(studentClub);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 ID가 주어지면")
        class Context_with_student_club_not_found {

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // given
                Long clubId = 999L;
                given(studentClubRepository.findById(clubId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.getReceiptsByClubForStudent(clubId))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_STUDENT_CLUB);

                then(studentClubRepository).should().findById(clubId);
            }
        }
    }

    @Nested
    @DisplayName("getReceiptById 메서드는")
    class Describe_getReceiptById {

        @Nested
        @DisplayName("유효한 영수증 ID가 주어지면")
        class Context_with_valid_receipt_id {

            @Test
            @DisplayName("해당 영수증을 반환한다")
            void it_returns_receipt() {
                // given
                Long receiptId = receipt.getId();
                ReceiptDto receiptDto = createReceiptDto(receipt.getId(), receipt.getContent(), receipt.getDeposit(), 0);

                given(receiptRepository.findById(receiptId)).willReturn(Optional.of(receipt));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);

                // when
                ReceiptDto result = receiptService.getReceiptById(receiptId);

                // then
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(receiptDto);

                then(receiptRepository).should().findById(receiptId);
                then(receiptMapper).should().toReceiptDto(receipt);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 영수증 ID가 주어지면")
        class Context_with_receipt_not_found {

            @Test
            @DisplayName("NOT_FOUND_RECEIPT 예외를 던진다")
            void it_throws_not_found_receipt_exception() {
                // given
                Long wrongReceiptId = 999L;
                given(receiptRepository.findById(wrongReceiptId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.getReceiptById(wrongReceiptId))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_RECEIPT);

                then(receiptRepository).should().findById(wrongReceiptId);
            }
        }
    }

    @Nested
    @DisplayName("deleteReceipt 메서드는")
    class Describe_deleteReceipt {

        @Nested
        @DisplayName("유효한 영수증 ID가 주어지면")
        class Context_with_valid_receipt_id {

            @Test
            @DisplayName("영수증을 성공적으로 삭제한다")
            void it_deletes_successfully() {
                // given
                Long receiptId = receipt.getId();
                Long clubId = studentClub.getId();
                ReceiptDto receiptDto = createReceiptDto(receipt.getId(), receipt.getContent(), receipt.getDeposit(), 0);
                String cachePattern = "receiptList::" + clubId + ":*";

                ReceiptRepository.ReceiptCount mockCount = mock(ReceiptRepository.ReceiptCount.class);
                given(mockCount.getTotal()).willReturn(10L);
                given(mockCount.getVerified()).willReturn(5L);

                given(receiptRepository.findById(receiptId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);
                given(studentClubRepository.findById(receipt.getStudentClub().getId())).willReturn(Optional.of(studentClub));
                given(receiptRepository.countTotalAndVerified(studentClub)).willReturn(mockCount);
                given(redisTemplate.keys(cachePattern)).willReturn(java.util.Set.of("receiptList::" + clubId + ":p0_s10_null_null"));

                // when
                ReceiptDto result = receiptService.deleteReceipt(receiptId, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result).isEqualTo(receiptDto);
                assertThat(user.getUserId()).isEqualTo(currentUser.getUsername());

                then(receiptRepository).should().findById(receiptId);
                then(receiptRepository).should().delete(receipt);
                then(studentClubRepository).should().save(studentClub);
                then(receiptMapper).should().toReceiptDto(receipt);
                then(receiptRepository).should().countTotalAndVerified(studentClub);
                then(redisTemplate).should().keys(cachePattern);
                then(redisTemplate).should().delete(any(java.util.Set.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 영수증 ID가 주어지면")
        class Context_with_receipt_not_found {

            @Test
            @DisplayName("NOT_FOUND_RECEIPT 예외를 던진다")
            void it_throws_not_found_receipt_exception() {
                // given
                Long wrongReceiptId = 999L;
                given(receiptRepository.findById(wrongReceiptId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.deleteReceipt(wrongReceiptId, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_RECEIPT);

                then(receiptRepository).should().findById(wrongReceiptId);
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                Long receiptId = receipt.getId();
                given(receiptRepository.findById(receiptId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> receiptService.deleteReceipt(receiptId, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(receiptRepository).should().findById(receiptId);
            }
        }
    }

    @Nested
    @DisplayName("updateReceipt 메서드는")
    class Describe_updateReceipt {

        @Nested
        @DisplayName("영수증 내용을 수정하면")
        class Context_with_content_update {

            @Test
            @DisplayName("내용이 성공적으로 수정된다")
            void it_updates_content_successfully() {
                // given
                Long existingId = receipt.getId();
                Long clubId = studentClub.getId();
                String cachePattern = "receiptList::" + clubId + ":*";
                ReceiptDto updateDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .date(receipt.getDate())
                        .content("수정된 내용")
                        .deposit(receipt.getDeposit())
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(updateDto);
                given(redisTemplate.keys(cachePattern)).willReturn(java.util.Set.of("receiptList::" + clubId + ":p0_s10_null_null"));

                // when
                ReceiptDto result = receiptService.updateReceipt(updateDto, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo("수정된 내용");
                assertThat(result.getDeposit()).isEqualTo(1000);

                then(receiptRepository).should().findById(existingId);
                then(studentClubRepository).should().save(studentClub);
                then(receiptRepository).should().save(receipt);
                then(redisTemplate).should().keys(cachePattern);
                then(redisTemplate).should().delete(any(java.util.Set.class));
            }
        }

        @Nested
        @DisplayName("영수증 금액을 수정하면")
        class Context_with_amount_update {

            @Test
            @DisplayName("금액이 성공적으로 수정된다")
            void it_updates_amount_successfully() {
                // given
                Long existingId = receipt.getId();
                Long clubId = studentClub.getId();
                String cachePattern = "receiptList::" + clubId + ":*";
                ReceiptDto updateDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .date(receipt.getDate())
                        .content(receipt.getContent())
                        .deposit(4500)
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(updateDto);
                given(redisTemplate.keys(cachePattern)).willReturn(java.util.Set.of("receiptList::" + clubId + ":p0_s10_null_null"));

                // when
                ReceiptDto result = receiptService.updateReceipt(updateDto, currentUser);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo(receipt.getContent());
                assertThat(result.getDeposit()).isEqualTo(4500);

                then(receiptRepository).should().findById(existingId);
                then(studentClubRepository).should().save(studentClub);
                then(receiptRepository).should().save(receipt);
                then(redisTemplate).should().keys(cachePattern);
                then(redisTemplate).should().delete(any(java.util.Set.class));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 영수증 ID가 주어지면")
        class Context_with_receipt_not_found {

            @Test
            @DisplayName("NOT_FOUND_RECEIPT 예외를 던진다")
            void it_throws_not_found_receipt_exception() {
                // given
                Long wrongReceiptId = 999L;
                ReceiptDto receiptDto = createReceiptDto(wrongReceiptId, "테스트", 1000, 0);
                given(receiptRepository.findById(wrongReceiptId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.updateReceipt(receiptDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_RECEIPT);

                then(receiptRepository).should().findById(wrongReceiptId);
            }
        }

        @Nested
        @DisplayName("다른 소속의 유저가 접근하면")
        class Context_with_unauthorized_user {

            @Test
            @DisplayName("NO_AUTHORIZATION_BELONGING 예외를 던진다")
            void it_throws_no_authorization_exception() {
                // given
                Long existingId = receipt.getId();
                ReceiptDto updateDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .date(receipt.getDate())
                        .content("수정된 내용")
                        .deposit(receipt.getDeposit())
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(anotherCurrentUser.getUsername())).willReturn(Optional.of(anotherUser));

                // when & then
                assertThatThrownBy(() -> receiptService.updateReceipt(updateDto, anotherCurrentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NO_AUTHORIZATION_BELONGING);

                then(receiptRepository).should().findById(existingId);
            }
        }

        @Nested
        @DisplayName("입금과 출금이 모두 입력되면")
        class Context_with_both_deposit_and_withdrawal {

            @Test
            @DisplayName("DUPLICATED_FLOW 예외를 던진다")
            void it_throws_duplicated_flow_exception() {
                // given
                Long existingId = receipt.getId();
                ReceiptDto receiptDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .content(receipt.getContent())
                        .deposit(1000)
                        .withdrawal(1000)
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.updateReceipt(receiptDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", DUPLICATED_FLOW);
            }
        }

        @Nested
        @DisplayName("입금과 출금이 모두 0이면")
        class Context_with_both_zero {

            @Test
            @DisplayName("DUPLICATED_FLOW 예외를 던진다")
            void it_throws_duplicated_flow_exception() {
                // given
                Long existingId = receipt.getId();
                ReceiptDto receiptDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .content(receipt.getContent())
                        .deposit(0)
                        .withdrawal(0)
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.updateReceipt(receiptDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", DUPLICATED_FLOW);
            }
        }

        @Nested
        @DisplayName("영수증 내용이 비어있으면")
        class Context_with_empty_content {

            @Test
            @DisplayName("EMPTY_CONTENT 예외를 던진다")
            void it_throws_empty_content_exception() {
                // given
                Long existingId = receipt.getId();
                ReceiptDto updateDto = ReceiptDto.builder()
                        .receiptId(receipt.getId())
                        .date(receipt.getDate())
                        .content(" ")
                        .deposit(receipt.getDeposit())
                        .build();

                given(receiptRepository.findById(existingId)).willReturn(Optional.of(receipt));
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.updateReceipt(updateDto, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", EMPTY_CONTENT);
            }
        }
    }

    @Nested
    @DisplayName("searchReceiptByKeyword 메서드는")
    class Describe_searchReceiptByKeyword {

        @Nested
        @DisplayName("유효한 키워드가 주어지면")
        class Context_with_valid_keyword {

            @Test
            @DisplayName("키워드를 포함한 영수증을 반환한다")
            void it_returns_receipts_with_keyword() {
                // given
                String keyword = "테스트";
                Receipt receipt = createReceipt(1L, "테스트 영수증", 0, 0, studentClub);
                ReceiptDto receiptDto = createReceiptDto(1L, "테스트 영수증", 0, 0);

                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));
                given(receiptRepository.findByStudentClubAndContent(studentClub.getId(), keyword))
                        .willReturn(List.of(receipt));
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);

                // when
                List<ReceiptDto> result = receiptService.searchReceiptByKeyword(keyword, currentUser);

                // then
                assertThat(result).isNotNull()
                        .hasSize(1);
                assertThat(result.get(0)).isEqualTo(receiptDto);
            }
        }

        @Nested
        @DisplayName("2글자 미만의 키워드가 주어지면")
        class Context_with_invalid_keyword {

            @Test
            @DisplayName("INVALID_KEYWORD 예외를 던진다")
            void it_throws_invalid_keyword_exception() {
                // given
                String invalidKeyword = "a";
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.of(user));

                // when & then
                assertThatThrownBy(() -> receiptService.searchReceiptByKeyword(invalidKeyword, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", INVALID_KEYWORD);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저가 검색하면")
        class Context_with_user_not_found {

            @Test
            @DisplayName("NOT_FOUND_USER 예외를 던진다")
            void it_throws_not_found_user_exception() {
                // given
                String keyword = "테스트";
                given(userRepository.findByUserId(currentUser.getUsername())).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.searchReceiptByKeyword(keyword, currentUser))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_USER);
            }
        }
    }

    @Nested
    @DisplayName("getReceiptsByClubPaging 메서드는")
    class Describe_getReceiptsByClubPaging {

        @Nested
        @DisplayName("유효한 페이징 요청이 주어지면")
        class Context_with_valid_paging_request {

            @Test
            @DisplayName("정렬 조건에 맞게 페이징된 영수증 목록을 반환한다")
            void it_returns_paged_receipts_with_sorting() {
                // given
                Long clubId = studentClub.getId();
                int page = 0;
                int size = 10;

                List<Receipt> receipts = List.of(receipt);
                Page<Receipt> receiptPage = new PageImpl<>(receipts, PageRequest.of(page, size), 1);
                ReceiptDto receiptDto = createReceiptDto(receipt.getId(), receipt.getContent(), receipt.getDeposit(), 0);

                given(studentClubRepository.findById(clubId)).willReturn(Optional.of(studentClub));
                given(receiptRepository.findByStudentClub(eq(studentClub), any(Pageable.class)))
                        .willReturn(receiptPage);
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);

                // when
                PagingReceiptDto result = receiptService.getReceiptsByClubPaging(clubId, page, size, null, null);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getReceiptDtoList()).hasSize(1);
                assertThat(result.getReceiptDtoList().get(0).getContent()).isEqualTo(receipt.getContent());

                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                then(receiptRepository).should().findByStudentClub(eq(studentClub), pageableCaptor.capture());

                Pageable capturedPageable = pageableCaptor.getValue();
                Sort sort = capturedPageable.getSort();

                assertThat(sort.getOrderFor("date")).isNotNull();
                assertThat(sort.getOrderFor("date").getDirection()).isEqualTo(Sort.Direction.DESC);
                assertThat(sort.getOrderFor("id")).isNotNull();
                assertThat(sort.getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
            }
        }

        @Nested
        @DisplayName("연도와 월 필터가 주어지면")
        class Context_with_date_filter {

            @Test
            @DisplayName("해당 기간의 영수증만 반환한다")
            void it_returns_filtered_receipts() {
                // given
                Long clubId = studentClub.getId();
                int page = 0;
                int size = 10;
                Integer year = 2025;
                Integer month = 5;

                List<Receipt> receipts = List.of(receipt);
                Page<Receipt> receiptPage = new PageImpl<>(receipts, PageRequest.of(page, size), 1);
                ReceiptDto receiptDto = createReceiptDto(receipt.getId(), receipt.getContent(), 0, 0);

                given(studentClubRepository.findById(clubId)).willReturn(Optional.of(studentClub));
                given(receiptRepository.findAllByStudentClubAndDateBetween(
                        eq(studentClub),
                        any(Date.class),
                        any(Date.class),
                        any(Pageable.class))
                ).willReturn(receiptPage);
                given(receiptMapper.toReceiptDto(receipt)).willReturn(receiptDto);

                // when
                PagingReceiptDto result = receiptService.getReceiptsByClubPaging(clubId, page, size, year, month);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getTotalElements()).isEqualTo(1);

                then(receiptRepository).should().findAllByStudentClubAndDateBetween(
                        eq(studentClub),
                        any(Date.class),
                        any(Date.class),
                        any(Pageable.class)
                );
                then(receiptRepository).should(never()).findByStudentClub(any(), any());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 학생회 ID가 주어지면")
        class Context_with_student_club_not_found {

            @Test
            @DisplayName("NOT_FOUND_STUDENT_CLUB 예외를 던진다")
            void it_throws_not_found_student_club_exception() {
                // given
                Long wrongClubId = 999L;
                int page = 0;
                int size = 10;

                given(studentClubRepository.findById(wrongClubId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> receiptService.getReceiptsByClubPaging(wrongClubId, page, size, null, null))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", 400)
                        .hasFieldOrPropertyWithValue("message", NOT_FOUND_STUDENT_CLUB);

                then(receiptRepository).should(never()).findByStudentClub(any(), any());
            }
        }
    }
}