package com.zln.javaguide.feignandribbontimout;
import com.zln.javaguide.common.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommonMistakesApplicationTimeoutByFeign {

    public static void main(String[] args) {
        Utils.loadPropertySource(FeignAndRibbonController.class, "default.properties");
        Utils.loadPropertySource(FeignAndRibbonController.class, "feign.properties");
        SpringApplication.run(CommonMistakesApplicationTimeoutByFeign.class, args);
    }
}

