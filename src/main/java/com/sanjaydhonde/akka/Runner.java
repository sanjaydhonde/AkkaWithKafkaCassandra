package com.sanjaydhonde.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import com.sanjaydhonde.akka.actors.KafkaSupervisorActor;
import com.sanjaydhonde.akka.spring.SpringExtension;

@Component
public class Runner implements CommandLineRunner {

    @Value("${num.of.consumers}")
    private int numOfConsumers;

    @Override
    public void run(String... strings) throws Exception {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.scan("com.sanjaydhonde.akka");
        ctx.refresh();

        // get the actor system from the spring context
        ActorSystem system = ctx.getBean(ActorSystem.class);

        ActorRef kafkaSupervisor = system.actorOf(SpringExtension.SpringExtProvider
                .get(system).props("KafkaSupervisorActor"), "kafka-supervisor");

        kafkaSupervisor.tell(new KafkaSupervisorActor.Start(numOfConsumers), ActorRef.noSender());
    }
}
