package com.example.demo.distribution;

import org.springframework.web.bind.annotation.*;

public interface Distribution {
    @PutMapping("/ride/c/{rid}")
    @ResponseBody
    Object channel(@PathVariable String rid, String driveruid, Double lon, Double lat, String numberPlate, String vehicleInfo);

    @PutMapping("/ride/cancel/{rid}")
    @ResponseBody
    Object cancel(@PathVariable String rid,String uid, Boolean cancel);
    @PostMapping("/ride")
    @ResponseBody
    Object createrid(String uid, Double pickUpLong,Double pickUpLat,String pickupaddress,
                     Double pickOffLong,Double pickOffLat,String destAddress, Integer type,
                                 String province, String city);
    @GetMapping("/ride/{rid}")
    @ResponseBody
    public Object infoSearch(@PathVariable String rid, Double lon,Double lat);
}
