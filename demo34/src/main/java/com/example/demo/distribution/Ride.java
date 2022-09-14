package com.example.demo.distribution;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Ride {
    private String rid;
    private Timestamp ctime;
    private String ruid;
    private String duid;
    private String topic;
    private String kind;
    private Double slat;
    private Double slng;
    private String saddress;
    private Double elat;
    private Double elng;
    private String eaddress;
    private String state;
    private Timestamp order_time;
    private Timestamp pick_time;
    private Timestamp arrive_time;
    private Timestamp cancel_time;
    private Double distance;
    private String oid;
    private Double rate;
    private String contexts;
}
