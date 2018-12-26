package com.sanjaydhonde.akka.store;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.sanjaydhonde.akka.data.TempData;

@Repository
public interface TempDataRepository extends CassandraRepository<TempData,String> {

    @Query("select * from TempData")
    Iterable<TempData> findAllTempData();

    @Query("select * from TempData where deviceid=?0 ")
    TempData findByDeviceid(String deviceId);

}