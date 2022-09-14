package com.example.demo.order;

import org.springframework.web.bind.annotation.*;

public interface Order {
    @PostMapping("/order")
    @ResponseBody
    Object createOrder(String rid);

    @GetMapping("/order/{oid}")
    @ResponseBody
    Object getOrderInfo(@PathVariable String oid,String uid);

    @PutMapping("/ride/{oid}")
    @ResponseBody
    public Object createPay(@PathVariable String oid, String uid, String platform);

    @PutMapping("/ride/confirm/{oid}")
    @ResponseBody
    public Object confirmPay(@PathVariable String oid,String uid,String platform,String trade_no);


}
