package cn.com.fanone.wopan.autoconfigure;

import cn.com.fanone.wopan.WoPanClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class WoPanAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WoPanAutoConfiguration.class));

    @Test
    void createsClientByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WoPanClient.class);
            assertThat(context).hasSingleBean(WoPanProperties.class);
        });
    }

    @Test
    void appliesConfiguredClientProperties() {
        contextRunner
                .withPropertyValues(
                        "wopan.access-token= access-token ",
                        "wopan.refresh-token= refresh-token ",
                        "wopan.auto-refresh=false",
                        "wopan.base-url=https://example.test/api",
                        "wopan.zone-url=https://upload.example.test",
                        "wopan.user-agent=wopan-test-agent",
                        "wopan.private-space-password=secret"
                )
                .run(context -> {
                    WoPanClient client = context.getBean(WoPanClient.class);
                    assertThat(ReflectionTestUtils.getField(client, "accessToken")).isEqualTo("access-token");
                    assertThat(ReflectionTestUtils.getField(client, "refreshToken")).isEqualTo("refresh-token");
                    assertThat(ReflectionTestUtils.getField(client, "autoRefresh")).isEqualTo(false);
                    assertThat(ReflectionTestUtils.getField(client, "baseUrl")).isEqualTo("https://example.test/api");
                    assertThat(ReflectionTestUtils.getField(client, "zoneUrl")).isEqualTo("https://upload.example.test");
                    assertThat(ReflectionTestUtils.getField(client, "userAgent")).isEqualTo("wopan-test-agent");
                    assertThat(ReflectionTestUtils.getField(client, "privateSpacePassword")).isEqualTo("secret");
                });
    }

    @Test
    void canDisableAutomaticClientBean() {
        contextRunner
                .withPropertyValues("wopan.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WoPanClient.class));
    }

    @Test
    void backsOffWhenApplicationProvidesClient() {
        contextRunner
                .withUserConfiguration(CustomClientConfiguration.class)
                .run(context -> assertThat(context.getBean(WoPanClient.class))
                        .isSameAs(context.getBean(CustomClientConfiguration.class).client));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomClientConfiguration {

        private final WoPanClient client = WoPanClient.builder().build();

        @Bean(destroyMethod = "close")
        WoPanClient customClient() {
            return client;
        }
    }
}
