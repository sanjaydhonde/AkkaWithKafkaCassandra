package com.sanjaydhonde.akka.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.sanjaydhonde.akka.spring.SpringExtension.SpringExtProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component("KafkaConsumerActor")
@Scope("prototype")
public class KafkaConsumerActor extends AbstractLoggingActor {

    private KafkaConsumer<String, String> consumer;

    @Value("${kafka.brokers}")
    private String brokers;

    @Value("${kafka.consumer.topics}")
    private String topics;

    private Map<ConsumerRecord<String, String>, ActorRef> actorRefMap = new HashMap<>();
    private AtomicInteger count;
    private int messageCount;

    public KafkaConsumerActor() {
        receive(ReceiveBuilder
                .match(Subscribe.class, this::subscribe)
                .match(Poll.class, this::poll)
                .match(Done.class, this::messageHandled)
                .match(RecoverableError.class, this::handleRecoverableError)
                .build()
        );
    }

    private void subscribe(Subscribe subscribe) {
        log().info("Starting consumer");

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", "asynctest");
        props.put("offset.storage", "kafka");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try {
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(topics.split(",")), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    doCommitSync();
                }
                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {}
            });
            context().sender().tell(new SubscriptionSuccess(), self());
        } catch (Exception e) {
            context().sender().tell(new SubscriptionFailure(), self());
        }
    }

    private void poll(Poll poll) {
        ConsumerRecords<String, String> records = consumer.poll(2000);
        if(records.count() >0 )
        log().info("Records found- count " + records.count());

        if (records.count() > 0) {
            count = new AtomicInteger(0);
            messageCount = records.count();
            records.forEach(record -> {
                ActorRef handler = context().actorOf(SpringExtProvider.get(context().system()).props("MessageHandler"));
                actorRefMap.put(record, handler);
                handler.tell(new MessageHandlerActor.ProcessMessage(record), self());
            });
        } else {
            self().tell(new Poll(), self());
        }
    }

    private void messageHandled(Done done) {
        int currentCount = count.incrementAndGet();
        if (currentCount == messageCount) {
            self().tell(new Poll(), self());
        }
    }

    private void handleRecoverableError(RecoverableError error) {
        log().error("Got error while processing for " + error.consumerRecord);
    }

    private void doCommitSync() {
        try {
            consumer.commitSync();
        } catch (WakeupException e) {
            doCommitSync();
            throw e;
        } catch (CommitFailedException e) {
            log().error("Commit failed", e);
        }
    }

    public static class Subscribe {}
    public static class Poll {}
    public static class SubscriptionSuccess {}
    public static class SubscriptionFailure {}
    public static class BaseMessage {
        ConsumerRecord consumerRecord;
        public BaseMessage(ConsumerRecord consumerRecord) {
            this.consumerRecord = consumerRecord;
        }
    }
    public static class Done extends BaseMessage {
        public Done(ConsumerRecord consumerRecord) {
            super(consumerRecord);
        }
    }
    public static class RecoverableError extends BaseMessage {
        public RecoverableError(ConsumerRecord consumerRecord) {
            super(consumerRecord);
        }
    }
}
