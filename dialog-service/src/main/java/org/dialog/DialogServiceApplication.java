package org.dialog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
class DialogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DialogServiceApplication.class, args);
    }

}
