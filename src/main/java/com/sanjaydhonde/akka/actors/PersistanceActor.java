package com.sanjaydhonde.akka.actors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sanjaydhonde.akka.data.TempData;
import com.sanjaydhonde.akka.store.TempDataRepository;

import akka.actor.AbstractLoggingActor;
import akka.japi.pf.ReceiveBuilder;

@Component("Persistance")
@Scope("prototype")
public class PersistanceActor extends AbstractLoggingActor {

    @Autowired
    private TempDataRepository repository;

    public PersistanceActor() {
        receive(ReceiveBuilder
                .match(Insert.class, this::insert)
                .build()
        );
    }

    public void insert(Insert insert) {
        List<TempData> data = insert.temparatureList;
        for(TempData reading:data) {
        	
        	this.repository.save(reading);
        }
        context().sender().tell(new InsertSuccessful(insert), self());
    }

    public static class Insert {
        public List<TempData> temparatureList;
        public MessageHandlerActor.ProcessMessage message;

        public Insert(List<TempData> temparatureList, MessageHandlerActor.ProcessMessage message) {
            this.temparatureList = temparatureList;
            this.message = message;
        }
    }

    public static class InsertSuccessful {
        public Insert insert;

        public InsertSuccessful(Insert insert) {
            this.insert = insert;
        }
    }

    public static class InsertFailed {
        Insert insert;

        public InsertFailed(Insert insert) {
            this.insert = insert;
        }
    }
}
