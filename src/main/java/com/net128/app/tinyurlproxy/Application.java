package com.net128.app.tinyurlproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class Application {
    static {
        String addLoc="spring.config.additional-location";
        if(System.getProperty(addLoc)==null) {
            String userHome=System.getProperty("user.home");
            String location=userHome+"/"+Application.class.getPackage().getName()+".properties";
            System.setProperty(addLoc, location);
        }
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
