package com.example.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing을 활성화하기 위한 스프링 구성 클래스
 * JPA Auditing을 사용하면 엔티티가 생성되거나 수정될 때 자동으로 날짜, 시간, 사용자 등의 감사 정보(audit information)를 캡처할 수 있다.
 * ex) @CreatedDate, @LastModifiedDate, @CreatedBy 등
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {

}
