package com.example.account.repository;

import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
    // JpaRepository<AccountUser, Long>을 확장함으로써 AccountUser 엔티티에 대한 기본적인 CRUD 연산과
    // 페이징 처리 기능을 자동으로 사용할 수 있음. AccountUser의 ID 타입은 Long임.

    // 필요한 경우 추가적인 메서드를 여기에 선언하여 사용할 수 있음. 예를 들어, 사용자 이름으로 검색하는 기능 등
    // 스프링 데이터 JPA는 메서드 이름을 해석하여 해당하는 SQL 쿼리를 자동으로 생성함.
}
