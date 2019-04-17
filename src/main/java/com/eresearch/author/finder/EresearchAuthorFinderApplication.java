package com.eresearch.author.finder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.eresearch.author.finder.application.event.listener.ApplicationEnvironmentPreparedEventListener;
import com.eresearch.author.finder.application.event.listener.ApplicationFailedEventListener;
import com.eresearch.author.finder.application.event.listener.ApplicationReadyEventListener;
import com.eresearch.author.finder.application.event.listener.ApplicationStartedEventListener;
import com.eresearch.author.finder.application.event.listener.BaseApplicationEventListener;
import com.eresearch.author.finder.db.DbOperations;

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

    @Autowired
    private DbOperations dbOperations;

    @Override
    public void run(String... strings) throws Exception {
        dbOperations.runTask();
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        //NOTE: add your scenarios if needed.
    }

}
