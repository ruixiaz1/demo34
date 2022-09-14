package com.example.demo.journal;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Journal {
    private Timestamp time;
    private String module;
    private String ranks;
    private String contexts;
}
