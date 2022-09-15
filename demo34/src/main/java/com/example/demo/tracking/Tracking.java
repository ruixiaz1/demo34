package com.example.demo.tracking;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.distribution.Ride;
import com.example.demo.journal.Journal;
import com.example.demo.mapper.JournalMapper;
import com.example.demo.mapper.RideMapper;
import com.example.demo.mapper.TrailMapper;
import com.example.demo.mqtt.Mqtts;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class Tracking {
    private String rid;
    private Mqtts mqtt;
    @Autowired
    private JournalMapper journalMapper;

    @Autowired
    private TrailMapper trailMapper;

    @Autowired
    private RideMapper rideMapper;

    private MqttCallback getTrackCallback(){
        MqttCallback mqttCallback=new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                try{
                    mqtt.connect();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            /*
            mqtt中message json格式
            {identity：0，1
             state:0(前往接乘客),1(接到乘客),2(行程中),3(结束订单)
             rid:
             alt:
             lat:
             lng:
             speed:
             }
             */
            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                JSONObject jsonObject=JSON.parseObject(mqttMessage.toString());
                System.out.print(jsonObject);
                JSONObject result=new JSONObject();
                Integer state=(Integer)jsonObject.get("state");
                String rid=jsonObject.get("rid").toString();
                UpdateWrapper<Ride> updateWrapper1=new UpdateWrapper<>();
                if(state==1){
                    updateWrapper1.eq("rid",rid)
                                  .set("pick_time",new Timestamp(System.currentTimeMillis()))
                                  .set("state","行程中");
                    rideMapper.update(null,updateWrapper1);

                }
                else if(state==3){
                    updateWrapper1.eq("rid",rid)
                                  .set("arrive_time",new Timestamp(System.currentTimeMillis()))
                                  .set("state","行程结束");
                    rideMapper.update(null,updateWrapper1);
                }
                String lat=jsonObject.get("lat").toString();
                String lng=jsonObject.get("lng").toString();
                String speed=jsonObject.get("speed").toString();
                String alt=jsonObject.get("alt").toString();
                Timestamp time=new Timestamp(System.currentTimeMillis());
                result.put("lat",lat);
                result.put("lng",lng);
                Trail trail=trailMapper.selectOne(new QueryWrapper<Trail>().eq("rid",rid));
                UpdateWrapper<Trail> updateWrapper=new UpdateWrapper<>();
                updateWrapper.eq("rid",rid)
                             .set("heighttrail",trail.getHeighttrail()+","+alt)
                             .set("gpstrail",trail.getGpstrail()+","+result.toJSONString())
                             .set("speedtrail",trail.getSpeedtrail()+","+speed)
                             .set("serialtime",trail.getSerialtime()+","+time);
                trailMapper.update(null,updateWrapper);
                journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Track",
                        "INFO","The trail information of "+rid+ "has been paid updated"));
                return;
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
        return mqttCallback;
    }
    public void startTracking(String channel,String rid){
        mqtt=new Mqtts(getTrackCallback());
        this.rid=rid;
        trailMapper.insert(new Trail("",rid,"","","",""));
        mqtt.subscribe(channel,1);
    }


}
