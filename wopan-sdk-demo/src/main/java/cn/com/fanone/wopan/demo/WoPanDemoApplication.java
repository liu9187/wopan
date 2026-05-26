package cn.com.fanone.wopan.demo;

import cn.com.fanone.wopan.demo.config.WoPanProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WoPanProperties.class)
public class WoPanDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WoPanDemoApplication.class, args);
    }
}
