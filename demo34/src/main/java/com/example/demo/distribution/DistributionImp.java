package com.example.demo.distribution;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.journal.Journal;
import com.example.demo.mapper.JournalMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.RideMapper;
import com.example.demo.mqtt.Mqtts;
import com.example.demo.order.Orders;
import com.example.demo.tracking.Tracking;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class DistributionImp implements Distribution{
    @Autowired
    private RideMapper rideMapper;
    @Autowired
    private Tracking track;
    private Mqtts mqtt;
    @Autowired
    private JournalMapper journalMapper;

    @Autowired
    private OrdersMapper ordersMapper;
    //生成随机rid
    private String getrid(){
        String str="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        String rid="";
        for(int i=0;i<8;i++){
            rid+=str.charAt((int)Math.floor(Math.random()*str.length()));
        }
        return rid;
    }

    private MqttCallback getdisCallback(){
        MqttCallback mqttCallback=new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                try{
                    mqtt.connect();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
        return mqttCallback;
    }

    @PutMapping("/ride/c/{rid}")
    @ResponseBody
    public JSONObject channel(@PathVariable String rid, String driveruid, Double lon, Double lat, String numberPlate,
                          String vehicleInfo){
        JSONObject result=new JSONObject();
        QueryWrapper<Ride> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("rid",rid);
        Ride ride = rideMapper.selectOne(queryWrapper);
        if(ride.getState().equals("接单")){
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                    "ERROR",driveruid+"fail to get rid "+rid));
            result.put("status","error code");
            result.put("msg","Ride has been accepted by others");
            return result;
        }
        //创建频道
        String channel= rid;
        track.startTracking(channel,rid);
        JSONObject result2=new JSONObject();
        //更新ride表内数据
        UpdateWrapper<Ride> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("rid",rid)
                .set("state","接单")
                .set("topic",channel)
                .set("duid",driveruid);
        rideMapper.update(null,updateWrapper);
        //封装结果信息
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                "INFO",driveruid+"success to get rid "+rid));
        result2.put("channel",channel);
        result.put("status",0);
        result.put("msg","Success");
        result.put("data",result2);
        return result;
    }

    @PutMapping("/ride/cancel/{rid}")
    @ResponseBody
    public JSONObject cancel(@PathVariable String rid,String uid, Boolean cancel){
        JSONObject result=new JSONObject();
        Ride ride=rideMapper.selectOne(new QueryWrapper<Ride>().eq("rid",rid));
        if(ride.getState().equals("取消")){
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                    "ERROR",uid+" fail to cancel ride "+rid));
            result.put("status","error code");
            result.put("msg","Ride has been cancelled");
            return result;
        }
        Timestamp t=new Timestamp(System.currentTimeMillis());
        UpdateWrapper<Ride> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("rid",rid)
                .set("state","取消")
                .set("cancel_time",t);
        rideMapper.update(null,updateWrapper);
        result.put("status",0);
        result.put("msg","Success");
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                "INFO",uid+" success to cancel ride "+rid));
       return result;
    }



    @PostMapping("/ride")
    @ResponseBody
    public Object createrid(String uid, Double pickUpLong,Double pickUpLat,String pickupaddress,
                                   Double pickOffLong,Double pickOffLat,String destAddress, Integer type,
                                   String province, String city){
        JSONObject jsonObject=new JSONObject();
        JSONObject message=new JSONObject();
        QueryWrapper<Ride> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("ruid",uid).orderByDesc("ctime");
        List<Ride> rides=rideMapper.selectList(queryWrapper);
        if(rides!=null){
            String oid=rideMapper.selectList(queryWrapper).get(0).getOid();
            QueryWrapper<Orders> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.eq("oid",oid);
            Orders orders=ordersMapper.selectOne(queryWrapper1);
            if(orders!=null&&orders.getState().equals("未支付")){
                journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                        "ERROR",uid+" has order unpaid"));
                jsonObject.put("status","error code");
                jsonObject.put("msg","Order not paid");
                return jsonObject;
            }
        }
        //获得创建时间
        Timestamp t=new Timestamp(System.currentTimeMillis());
        GlobalCoordinates start=new GlobalCoordinates(pickUpLat,pickUpLong);
        GlobalCoordinates dest=new GlobalCoordinates(pickOffLat,pickOffLong);
        Double distance=new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere,start,dest).getEllipsoidalDistance();
        //装载起点终点信息
        message.put("start",pickupaddress);
        message.put("end",destAddress);
        mqtt=new Mqtts(getdisCallback());
        String rid=getrid();
        String topic=province+city;
        String [] ckind={"jingji","shushi","haohua"};
        rideMapper.insert(new Ride(rid,t,uid,null,topic,ckind[type],
                        pickUpLat,pickUpLong,pickupaddress,pickOffLat,pickOffLong,destAddress,
                   "创建",null,null,null,null,distance,null,null,null));
        //进行派单投放
        mqtt.publish(topic,message.toJSONString(),2);
        jsonObject.put("status","0");
        jsonObject.put("msg","Success");
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                "INFO",uid+" success to create ride "+rid));
        return jsonObject;
    }
    
    @GetMapping("/ride/{rid}")
    @ResponseBody
    public JSONObject infoSearch(@PathVariable String rid, Double lon, Double lat){
        JSONObject result=new JSONObject();
        QueryWrapper<Ride> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("rid",rid);
        Ride ride=rideMapper.selectOne(queryWrapper);
        if(ride.getState().equals("取消")){
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                    "ERROR","the ride "+ride+" has been cancelled before"));
            result.put("status","error code");
            result.put("msg","cancelled");
            return result;
        }
        result.put("status",0);
        result.put("msg","Success");
        result.put("data",ride);
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Distribution",
                "INFO","the ride "+ride+" has been cancelled successfully"));

        return result;
    }
}
