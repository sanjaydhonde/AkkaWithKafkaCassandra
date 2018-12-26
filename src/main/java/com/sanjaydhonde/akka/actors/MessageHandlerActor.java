package com.sanjaydhonde.akka.actors;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.sanjaydhonde.akka.data.TempData;
import com.sanjaydhonde.akka.spring.SpringExtension;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

@Component("MessageHandler")
@Scope("prototype")
public class MessageHandlerActor extends AbstractLoggingActor {
	
    public MessageHandlerActor() {
        receive(ReceiveBuilder
                .match(ProcessMessage.class, this::handle)
                .match(PersistanceActor.InsertSuccessful.class, this::insertSuccessful)
                .match(PersistanceActor.InsertFailed.class, this::insertFailed)
                .build()
        );
    }

    private void handle(ProcessMessage processMessage) {
        try {
        	TempData temperature = new Gson().fromJson(processMessage.consumerRecord.value(), TempData.class);
            ActorRef persistActor = context().actorOf(SpringExtension.SpringExtProvider.get(context().system()).props("Persistance"));
            persistActor.tell(new PersistanceActor.Insert(Arrays.asList(temperature), processMessage), self());
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            log().error(e.toString(), e);
            context().parent().tell(new KafkaConsumerActor.RecoverableError(processMessage.consumerRecord), self());
        }
    }
    
    private void insertSuccessful(PersistanceActor.InsertSuccessful insertSuccessful) {
        context().parent().tell(new KafkaConsumerActor.Done(insertSuccessful.insert.message.consumerRecord), self());
    }

    private void insertFailed(PersistanceActor.InsertFailed insertFailed) {
        // TODO: 
    }

    public static class ProcessMessage {
        ConsumerRecord<String, String> consumerRecord;

        public ProcessMessage(ConsumerRecord<String, String> consumerRecord) {
            this.consumerRecord = consumerRecord;
        }
    }
    
    public static class Successful {
        private ProcessMessage processMessage;

        public Successful(ProcessMessage processMessage) {
            this.processMessage = processMessage;
        }
    }
}
