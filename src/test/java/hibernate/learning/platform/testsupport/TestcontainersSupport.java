package hibernate.learning.platform.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;

import org.testcontainers.containers.PostgreSQLContainer;

public final class TestcontainersSupport {
    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16.4-alpine").withDatabaseName("learning_platform_test")
                .withUsername("test").withPassword("test");
        POSTGRES.start();
    }

    private TestcontainersSupport() {
    }

    public static PostgreSQLContainer<?> getContainer() {
        return POSTGRES;
    }

    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
