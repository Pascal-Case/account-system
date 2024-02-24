package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 이 인터페이스가 데이터 접근 계층의 컴포넌트임을 스프링에게 알림
public interface AccountRepository extends JpaRepository<Account, Long> {
    // ID 기준으로 가장 최근에 생성된 Account를 조회하는 메서드
    // 스프링 데이터 JPA는 메서드 이름을 분석하여 자동으로 쿼리를 생성함
    // 결과는 Optional로 감싸져 있어, 조회된 Account가 없을 경우 null 대신 Optional.empty() 반환
    Optional<Account> findFirstByOrderByIdDesc();
    
    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String AccountNumber);
}
