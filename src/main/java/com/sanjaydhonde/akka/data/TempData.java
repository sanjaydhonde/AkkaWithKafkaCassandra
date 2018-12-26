package com.sanjaydhonde.akka.data;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public class TempData {
	@PrimaryKeyColumn(name = "deviceid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String deviceid;
	
	@PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID id;
	
	@Column(value="temperature")
    private Double temperature;

    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

    public String getDeviceid() {
        return deviceid;
    }

}
