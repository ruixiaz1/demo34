package com.example.demo.mqtt;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

@Slf4j
@Data
public class Mqtts {
    private MqttClient client;
//    @Value("${spring.mqtt.username}")
    private String userName;
//    @Value("${spring.mqtt.password}")
    private String password;
//    @Value("${spring.mqtt.host}")
    private String host;
//    @Value("${spring.mqtt.clientId}")
    private String clientId;
//    @Value("${spring.mqtt.timeout}")
    private int timeout;
//    @Value("${spring.mqtt.keepalive}")
    private int keepAlive;

    public static MqttCallback mqttCallback;

    public Mqtts(MqttCallback mq){
        host="tcp://0.0.0.0:1883";
        clientId="emqx_OTY3OD";
        userName="admin";
        password="public";
        timeout=1000;
        keepAlive = 10;
        mqttCallback=mq;
        try{
            this.connect();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void subscribe(String topic,int qos){
        try{
            client.subscribe(topic,qos);
            log.info("订阅主题:{}",topic);
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void publish(String topic, String pushMessage,int qos){
        publish(qos, false,topic, pushMessage);
    }
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = client.getTopic(topic);
        if (null == mTopic) {
            log.error("主题不存在:{}",mTopic);
        }
        try {
            mTopic.publish(message);
        } catch (Exception e) {
            log.error("mqtt发送消息异常:",e);
        }
    }

    public void connect() throws Exception{
        if(client==null){
            client=new MqttClient(host, clientId, new MemoryPersistence());
        }
        MqttConnectOptions options=new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(timeout);

        options.setKeepAliveInterval(keepAlive);

        client.setCallback(mqttCallback);
        client.connect();
    }
}
