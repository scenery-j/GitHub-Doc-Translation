package com.gitdoc.translation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.data.redis.host=localhost",
        "github.app.id=test",
        "github.app.client-id=test",
        "github.app.client-secret=test",
        "github.app.private-key-path=./test-key.pem",
        "github.app.webhook-secret=test",
        "spring.ai.openai.api-key=test",
        "app.jwt.secret=test-secret-key-at-least-32-chars-long",
        "app.encryption.key=test-encryption-key-32-chars!!!!"
})
class DocTranslationApplicationTests {

    @Test
    void contextLoads() {
        // Basic smoke test
    }
}
