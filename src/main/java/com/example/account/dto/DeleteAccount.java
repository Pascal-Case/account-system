package com.example.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 계좌 생성 요청 및 응답을 위한 DTO 클래스.
 * 요청 데이터와 응답 데이터 구조를 포함한다.
 */
public class DeleteAccount {

    /**
     * 계좌 생성 요청 데이터를 담는 내부 클래스.
     * 사용자 ID와 초기 잔액 정보를 포함한다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId;
        private String accountNumber;
        private LocalDateTime unRegisteredAt;

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .unRegisteredAt(accountDto.getUnRegisteredAt())
                    .build();
        }
    }
}
