package com.sanjaydhonde.akka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.sanjaydhonde.akka.spring.SpringExtension;

import akka.actor.ActorSystem;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = ActorSystem.create("AKC");
        SpringExtension.SpringExtProvider.get(actorSystem).initialize(applicationContext);
        return actorSystem;
    }
}
