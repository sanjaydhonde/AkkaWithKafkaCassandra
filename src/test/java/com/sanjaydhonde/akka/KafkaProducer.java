package com.sanjaydhonde.akka;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.Gson;
import com.sanjaydhonde.akka.data.TempData;

public class KafkaProducer {
	
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "13.127.109.239:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=1000;i<1100;i++ ) {
                	TempData reading = new TempData();
                	reading.setDeviceid(String.valueOf(randomDeviceId(1000,5000)));
                	reading.setId(UUIDs.timeBased());
                	reading.setTemperature(randomTemperature(30,40));
                    System.out.println("Sending message"+new Gson().toJson(reading));
                                       
                    producer.send(new ProducerRecord<>("akka-test", String.valueOf(i), new Gson().toJson(reading)));
                    try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        }producer.close();
    }
   
    public static long randomDeviceId(int min, int max)
    {
         return ThreadLocalRandom.current().nextLong(min, max);
    }
    
    public static double randomTemperature(int min, int max)
    {
    	return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
