package com.demo.buildserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "start_time")
    private Long startTime;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "type")
    private String type;

    @Column(name = "host")
    private String host;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "alert")
    private String alert;

    @Override
    public String toString() {
        return "{ id:'" + id + '\'' +
                " | type:'" + type + '\'' +
                " | host:'" + host + '\'' +
                " | duration:'" + duration + '\'' +
                " | alert:'" + alert + '\'' + " }";
    }
}
