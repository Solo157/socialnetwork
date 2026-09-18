package org.counter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
class DialogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DialogServiceApplication.class, args);
    }

}
