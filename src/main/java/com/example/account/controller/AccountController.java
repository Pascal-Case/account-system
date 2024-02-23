package com.example.account.controller;

import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스를 REST 컨트롤러로 선언
@RequiredArgsConstructor // Lombok을 사용하여 final 필드에 대한 생성자를 자동으로 생성
public class AccountController {
    private final AccountService accountService; // 계좌 서비스 의존성 주입

    @PostMapping("/account")
    // '/account' 경로로 POST 요청이 오면 이 메서드를 호출
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
            // HTTP 요청 본문을 CreateAccount.Request 객체로 변환하고 검증
    ) {
        accountService.createAccount(
                request.getUserId(),
                request.getInitialBalance()
                // 서비스 레이어에 계좌 생성 로직을 위임
        );
        return null; // 현재는 null을 반환하지만, 실제로는 작업 결과에 따라 응답 객체 반환 예정
    }
}
