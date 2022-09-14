package com.example.demo.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Trail {
    private String uid;
    private String rid;
    private String serialtime;
    private String gpstrail;
    private String speedtrail;
    private String heighttrail;
}
