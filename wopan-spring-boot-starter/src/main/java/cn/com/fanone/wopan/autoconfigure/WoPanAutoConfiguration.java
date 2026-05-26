package cn.com.fanone.wopan.autoconfigure;

import cn.com.fanone.wopan.WoPanClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(WoPanClient.class)
@EnableConfigurationProperties(WoPanProperties.class)
@ConditionalOnProperty(prefix = "wopan", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WoPanAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(WoPanClient.class)
    public WoPanClient woPanClient(
            WoPanProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        WoPanClient.Builder builder = properties.applyTo(WoPanClient.builder());
        objectMapperProvider.ifAvailable(builder::objectMapper);
        return builder.build();
    }
}
