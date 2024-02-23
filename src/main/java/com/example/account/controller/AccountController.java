package com.example.account.controller;

import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
// 스프링에게 이 클래스가 REST API를 처리하는 컨트롤러임을 알림, 응답 본문이 자동으로 JSON 등으로 변환됨
@RequiredArgsConstructor
// Lombok을 사용하여 final이나 @NonNull 필드에 대한 생성자를 자동으로 생성, 의존성 주입을 위해 사용
public class AccountController {
    private final AccountService accountService; // 계좌 생성과 관련된 비즈니스 로직을 처리하는 서비스 레이어에 대한 의존성 주입

    @PostMapping("/account")
    // HTTP POST 요청을 '/account' URL 경로로 매핑, 새 계좌 생성 요청을 처리
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
            // 클라이언트로부터 받은 JSON 요청 본문을 CreateAccount.Request DTO 객체로 변환 및 검증
    ) {
        // AccountService를 통해 계좌 생성 로직 실행, 입력 받은 사용자 ID와 초기 잔액으로 계좌 생성
        // 생성된 계좌 정보를 CreateAccount.Response DTO로 변환하여 클라이언트에 반환
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()
                )
        );
    }
}
