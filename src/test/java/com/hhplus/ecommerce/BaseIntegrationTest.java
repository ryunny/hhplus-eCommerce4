package com.hhplus.ecommerce;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Testcontainers 기반 통합 테스트 Base 클래스
 *
 * MySQL Testcontainer를 사용하여 실제 DB 환경에서 테스트를 수행합니다.
 * 모든 통합 테스트는 이 클래스를 상속받아 작성합니다.
 */
@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    private DataSource dataSource;

    /**
     * Spring Boot의 DataSource 설정을 Testcontainer의 MySQL로 동적 설정
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    /**
     * 각 테스트 전에 DB 초기화
     * 테스트 간 데이터 격리를 보장합니다.
     */
    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
    }

    /**
     * 데이터베이스 초기화
     * 모든 테이블의 데이터를 삭제하고 AUTO_INCREMENT를 리셋합니다.
     */
    private void cleanDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // 외래 키 체크 비활성화
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 모든 테이블 초기화
            statement.execute("TRUNCATE TABLE order_items");
            statement.execute("TRUNCATE TABLE payments");
            statement.execute("TRUNCATE TABLE orders");
            statement.execute("TRUNCATE TABLE cart_items");
            statement.execute("TRUNCATE TABLE user_coupons");
            statement.execute("TRUNCATE TABLE coupon_queues");
            statement.execute("TRUNCATE TABLE coupons");
            statement.execute("TRUNCATE TABLE products");
            statement.execute("TRUNCATE TABLE categories");
            statement.execute("TRUNCATE TABLE users");
            statement.execute("TRUNCATE TABLE refunds");

            // 외래 키 체크 활성화
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
