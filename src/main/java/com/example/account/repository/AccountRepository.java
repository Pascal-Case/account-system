package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // 이 인터페이스가 데이터 접근 계층의 컴포넌트임을 스프링에게 알림
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 스프링 데이터 JPA는 메서드 이름을 분석하여 자동으로 쿼리를 생성함
    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
