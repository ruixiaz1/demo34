package com.example.demo.mqtt;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

//@Component
@Data
//@Configuration
public class Mqtt {
    @Autowired
    private MqttPushClient mqttPushClient;

    //指定配置文件application-local.properties中的属性名前缀
    @Value("${spring.mqtt.username}")
    private String userName;
    @Value("${spring.mqtt.password}")
    private String password;
    @Value("${spring.mqtt.host}")
    private String host;
    @Value("${spring.mqtt.clientId}")
    private String clientId;
    @Value("${spring.mqtt.timeout}")
    private int timeout;
    @Value("${spring.mqtt.keepalive}")
    private int keepAlive;

    /**
     * 连接至mqtt服务器，获取mqtt连接
     * @return
     */
    public Mqtt(String clientId){
        this.clientId=clientId;
    }
    public MqttPushClient getMqttPushClient() {
        //连接至mqtt服务器，获取mqtt连接
        mqttPushClient.connect(host, clientId, userName, password, timeout, keepAlive);
        return mqttPushClient;
    }
}
