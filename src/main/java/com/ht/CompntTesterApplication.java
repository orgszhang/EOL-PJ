package com.ht;


import com.ht.swing.MainFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
public class CompntTesterApplication {

    private static final Log logger = LogFactory.getLog(CompntTesterApplication.class);

    public static void main(String[] args) {
        new SpringApplicationBuilder(CompntTesterApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    public MainFrame mainFrame() {
        logger.info("hi... i'am starting ... ");
        return new MainFrame(null,null);
    }
}