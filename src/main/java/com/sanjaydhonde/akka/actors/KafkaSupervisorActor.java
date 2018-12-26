package com.sanjaydhonde.akka.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sanjaydhonde.akka.spring.SpringExtension;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

@Component("KafkaSupervisorActor")
@Scope("prototype")
public class KafkaSupervisorActor extends AbstractLoggingActor {
    public static class Start {

        private int numOfConsumers;

        public Start(int numOfConsumers) {
            this.numOfConsumers = numOfConsumers;
        }
    }

    @Value("${num.of.consumers}")
    private int numOfConsumers;

    Map<Integer, ActorRef> workers = new HashMap<>();

    KafkaSupervisorActor() {
        receive(ReceiveBuilder
                .match(Start.class, this::startConsumers)
                .match(KafkaConsumerActor.SubscriptionSuccess.class, this::startConsumerPolling)
                .build()
        );
    }
    private void startConsumers(Start start) {
        log().info("Starting consumers");
        IntStream.range(0, start.numOfConsumers)
                .forEach(i -> {
                    ActorRef worker = context().actorOf(SpringExtension.SpringExtProvider.get(context().system()).props("KafkaConsumerActor"));
                    workers.put(i, worker);
                    worker.tell(new KafkaConsumerActor.Subscribe(), self());
                });
    }

    private void startConsumerPolling(KafkaConsumerActor.SubscriptionSuccess success) {
        sender().tell(new KafkaConsumerActor.Poll(), self());
    }
}
