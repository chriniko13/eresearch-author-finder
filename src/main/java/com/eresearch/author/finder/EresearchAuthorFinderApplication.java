package com.eresearch.author.finder;

import com.eresearch.author.finder.application.event.listener.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * This is the entry point for our microservice.
 *
 * @author chriniko
 */
@SpringBootApplication
public class EresearchAuthorFinderApplication implements CommandLineRunner, ApplicationRunner {

    public static void main(String[] args) {

        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(
                EresearchAuthorFinderApplication.class);

        registerApplicationListeners(springApplicationBuilder);

        springApplicationBuilder
                .web(true)
                .run(args);

    }


    private static void registerApplicationListeners(final SpringApplicationBuilder springApplicationBuilder) {

        springApplicationBuilder.listeners(new ApplicationEnvironmentPreparedEventListener());
        springApplicationBuilder.listeners(new ApplicationFailedEventListener());
        springApplicationBuilder.listeners(new ApplicationReadyEventListener());
        springApplicationBuilder.listeners(new ApplicationStartedEventListener());
        springApplicationBuilder.listeners(new BaseApplicationEventListener());

    }

    @Override
    public void run(String... strings) throws Exception {
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        //NOTE: add your scenarios if needed.
    }

}
