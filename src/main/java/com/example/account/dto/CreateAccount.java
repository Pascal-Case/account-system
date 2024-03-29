package com.example.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class CreateAccount {

    @Getter // Lombok 어노테이션으로 Getter 메서드 자동 생성
    @Setter // Lombok 어노테이션으로 Setter 메서드 자동 생성
    @NoArgsConstructor // Lombok 어노테이션으로 파라미터 없는 기본 생성자 자동 생성
    @AllArgsConstructor // Lombok 어노테이션으로 모든 필드를 포함하는 생성자 자동 생성
    public static class Request {
        @NotNull // 이 필드는 null일 수 없음을 명시
        @Min(1) // 이 필드의 최소값은 1임을 명시
        private Long userId; // 사용자의 고유 식별자

        @NotNull // 이 필드는 null일 수 없음을 명시
        @Min(0) // 이 필드의 최소값은 0임을 명시, 계좌의 초기 잔액을 의미
        private Long initialBalance; // 계좌의 초기 잔액
    }

    @Getter // Lombok 어노테이션으로 Getter 메서드 자동 생성
    @Setter // Lombok 어노테이션으로 Setter 메서드 자동 생성
    @NoArgsConstructor // Lombok 어노테이션으로 파라미터 없는 기본 생성자 자동 생성
    @AllArgsConstructor // Lombok 어노테이션으로 모든 필드를 포함하는 생성자 자동 생성
    @Builder // Lombok 어노테이션으로 빌더 패턴을 이용한 객체 생성을 지원
    public static class Response {
        private Long userId; // 사용자 ID
        private String accountNumber; // 계좌 번호
        private LocalDateTime registeredAt; // 계좌 등록 시간

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
