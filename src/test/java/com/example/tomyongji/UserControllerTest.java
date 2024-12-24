package com.example.tomyongji;

import com.example.tomyongji.admin.dto.ApiResponse;
import com.example.tomyongji.admin.entity.Member;
import com.example.tomyongji.admin.repository.MemberRepository;
import com.example.tomyongji.auth.controller.UserController;
import com.example.tomyongji.auth.dto.UserRequsetDto;
import com.example.tomyongji.auth.service.UserService;
import com.example.tomyongji.receipt.repository.StudentClubRepository;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Spy
    private StudentClubRepository studentClubRepository;
    @Spy
    private MemberRepository memberRepository;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    public void init(){
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        Member member = new Member();
        member.setName("투명지");
        member.setStudentNum("60222024");
        member.setStudentClub(studentClubRepository.findById(3L).get());
        memberRepository.save(member);
    }

    @DisplayName("회원 가입 성공")
    @Test
    void signUpSuccess() throws Exception {
        // given
        UserRequsetDto request = UserRequestDto();
        ApiResponse<Long> response = ApiResponse();

        doReturn(response).when(userService)
                .join(any(UserRequsetDto.class));
        //when
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/users/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request))
        );
        //then
        MvcResult mvcResult = (MvcResult) resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("statusCode", response.getStatusCode()).exists())
                .andExpect(jsonPath("statusMessage", response.getStatusMessage()).exists())
                .andExpect(jsonPath("data", response.getData()).exists());

    }
    private UserRequsetDto UserRequestDto(){
        return UserRequsetDto.builder()
                .userId("tomyongji")
                .name("투명지")
                .studentNum("60222024")
                .college("ICT융합대학")
                .studentClubId(3)
                .email("eeeseohyun@gmail.com")
                .password("Tomyongji2024!")
                .role("STU")
                .build();
    }

    private  ApiResponse<Long> ApiResponse(){
        long id = 57;
        return new ApiResponse<>(200,"회원가입에 성공하셨습니다.",id);
    }
}
