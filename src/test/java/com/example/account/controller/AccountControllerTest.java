package com.example.account.controller;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class) // AccountController에 대한 웹 계층 테스트 환경을 설정
class AccountControllerTest {
    @MockBean
    private AccountService accountService; // AccountService의 모의 객체를 생성

    @MockBean
    private RedisTestService redisTestService; // RedisTestService의 모의 객체를 생성

    @Autowired
    private MockMvc mockMvc; // Spring MVC 동작을 모의하는 MockMvc 객체를 주입

    @Autowired
    private ObjectMapper objectMapper; // JSON 객체와 Java 객체 간 변환을 처리하는 ObjectMapper 객체를 주입

    @Test
    void successCreateAccount() throws Exception {
        // given: AccountService의 createAccount 메서드가 호출될 때 반환될 AccountDto 객체를 설정
        // (어떠한 값이 들어와도 willReturn 이하의 AccountDto 객체를 반환)
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());

        // when & then: /account 엔드포인트로 POST 요청을 보내고 응답을 검증
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON) // 요청 타입을 JSON으로 설정
                        .content(objectMapper.writeValueAsString(
                                new CreateAccount.Request(1L, 100L) // 요청 본문에 해당하는 객체를 JSON 문자열로 변환
                        )))
                .andExpect(status().isOk()) // HTTP 상태 코드가 200(OK)인지 검증
                .andExpect(jsonPath("$.userId").value(1)) // JSON 응답 본문에서 userId가 1인지 검증
                .andExpect(jsonPath("$.accountNumber").value("1234567890")) // accountNumber가 "1234567890"인지 검증
                .andDo(print()); // 요청과 응답의 세부 사항을 콘솔에 출력
    }

    @Test
    void successDeleteAccount() throws Exception {
        // given
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());

        // when & then
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(1L, "1000000000")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
    }

    @Test
    void successGetAccountsByUserId() throws Exception {
        //given
        List<AccountDto> accountDtos = Arrays.asList(
                AccountDto.builder()
                        .accountNumber("1234567890")
                        .balance(1000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber("1234567891")
                        .balance(2000L)
                        .build(),
                AccountDto.builder()
                        .accountNumber("1234567892")
                        .balance(3000L)
                        .build()
        );
        given(accountService.getAccountsByUserId(anyLong()))
                .willReturn(accountDtos);
        //when
        //then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].balance").value("1000"));

    }
}
