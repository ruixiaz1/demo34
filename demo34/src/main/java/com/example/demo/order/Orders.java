package com.example.demo.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Orders {
    private String oid;
    private String rid;
    private Timestamp ctime;
    private Double total_price;
    private Double start_price;
    private Double time_fee;
    private Double distance_fee;
    private Double service_fee;
    private Double d_price;
    private String state;
    private String payment_platfrom;
    private String serial_number;
    private String result;
}
