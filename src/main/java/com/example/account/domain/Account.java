package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.example.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Lombok의 빌더 패턴 구현을 위한 어노테이션
@Entity // 이 클래스가 데이터베이스 테이블에 매핑되는 엔티티임을 나타냄
@EntityListeners(AuditingEntityListener.class) // 엔티티에 Auditing 기능을 추가
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne // Account 엔티티는 AccountUser 엔티티와 다대일 관계
    private AccountUser accountUser;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }
}
