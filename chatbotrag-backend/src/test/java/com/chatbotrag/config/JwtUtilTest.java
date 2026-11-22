package com.chatbotrag.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withBean(JwtUtil.class).withPropertyValues("jwt.expiration-ms=86400000");

    @Test
    void configuredKeySignsAndValidatesTokens() {
        context.withPropertyValues("jwt.secret=" + "a".repeat(64)).run(app -> {
            assertThat(app).hasNotFailed();
            JwtUtil jwt = app.getBean(JwtUtil.class);
            String token = jwt.generateToken("fixture@example.test", 7L);
            assertThat(jwt.validateToken(token)).isTrue();
            assertThat(jwt.extractEmail(token)).isEqualTo("fixture@example.test");
        });
    }

    @Test
    void blankKeyStopsStartup() {
        context.withPropertyValues("jwt.secret=").run(app -> assertThat(app).hasFailed());
    }

    @Test
    void shortKeyStopsStartup() {
        context.withPropertyValues("jwt.secret=short").run(app -> assertThat(app).hasFailed());
    }

    @Test
    void invalidTokenIsRejected() {
        context.withPropertyValues("jwt.secret=" + "b".repeat(64)).run(app -> {
            assertThat(app.getBean(JwtUtil.class).validateToken("invalid.fixture.token")).isFalse();
        });
    }
}
