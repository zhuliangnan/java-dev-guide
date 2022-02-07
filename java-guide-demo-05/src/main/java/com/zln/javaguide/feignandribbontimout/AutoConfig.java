package com.zln.javaguide.feignandribbontimout;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "zln.javaguide.feignandribbontimout")
public class AutoConfig {
}
