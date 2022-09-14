package com.example.demo.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.distribution.Ride;
import com.example.demo.journal.Journal;
import com.example.demo.mapper.JournalMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.RideMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

@Controller
public class OrderImp implements Order{
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private JournalMapper journalMapper;

    @Autowired
    private UserMapper userMapper;

    private String getoids(){
        String str="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        String rid="";
        for(int i=0;i<8;i++){
            rid+=str.charAt((int)Math.floor(Math.random()*str.length()));
        }
        return rid;
    }
    @PostMapping("/order")
    @ResponseBody
    public Object createOrder(String rid){
        JSONObject jsonObject=new JSONObject();
        //查找是否已创建过支付订单
        QueryWrapper<Orders> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("rid",rid);
        Orders orderss=ordersMapper.selectOne(queryWrapper1);
        if(orderss!=null){
            jsonObject.put("status","error code");
            jsonObject.put("msg","order has been created");
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                    "ERROR","Ride "+rid+" can not create order twice"));
            return jsonObject;
        }
        //获取创建时间
        Timestamp t=new Timestamp(System.currentTimeMillis());
        //查找ride订单
        QueryWrapper<Ride> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("rid",rid);
        Ride ride=rideMapper.selectOne(queryWrapper);
        //计算用户行程次数
        queryWrapper.clear();
        queryWrapper.eq("ruid",ride.getRuid());
        Long times=rideMapper.selectCount(queryWrapper);
        String oid=getoids();
        //起步价
        Double startprice=12.0;
        //时间费
        Long stime= ride.getArrive_time().getTime();
        Long etime=ride.getArrive_time().getTime();
        Long interval=(etime-stime)/(1000*60);
        Double timefee=0.75*interval;
        //路程费
        Double distancefee=2.5*ride.getDistance()/1000;
        //总价
        Double totalprice=startprice+timefee+distancefee;
        //动态价格
        Double dynamicfee=0.05*totalprice*times;

        ordersMapper.insert(new Orders(oid,rid,t,totalprice,startprice,
                            distancefee,timefee,0.0,dynamicfee,
                      "未支付",null,null,null));
        //更新ride表中oid的信息
        UpdateWrapper<Ride> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("rid",rid)
                .set("oid",oid);
        rideMapper.update(null,updateWrapper);
        jsonObject.put("status","0");
        jsonObject.put("msg","Success");
        JSONObject message=new JSONObject();
        message.put("oid",oid);
        jsonObject.put("data",message);
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                "INFO","Ride "+rid+" create order successfully"));
        return jsonObject;
    }

    @GetMapping("/order/{oid}")
    @ResponseBody
    public Object getOrderInfo(@PathVariable String oid,String uid){
        JSONObject jsonObject=new JSONObject();
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("uid",uid);
        User user=userMapper.selectOne(queryWrapper);
        if(user==null){
            jsonObject.put("status","error code");
            jsonObject.put("msg","Permission Denied");
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                    "WARNING","UID"+uid+"does not exist"));
            return jsonObject;
        }
        QueryWrapper<Orders> queryWrapper2=new QueryWrapper<>();
        queryWrapper2.eq("oid",oid);
        Orders order=ordersMapper.selectOne(queryWrapper2);
        jsonObject.put("status","0");
        jsonObject.put("msg","Success");
        jsonObject.put("data",order);
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                "INFO","Get order "+oid+" information successfully"));
        return jsonObject;
    }

    @PutMapping("/ride/{oid}")
    @ResponseBody
    public Object createPay(@PathVariable String oid, String uid, String platform){
        JSONObject jsonObject=new JSONObject();
        QueryWrapper<Orders> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("oid",oid).eq("state","已支付");
        if (ordersMapper.selectOne(queryWrapper)!=null){
            jsonObject.put("status","error code");
            jsonObject.put("msg","THis oid has been paid");
            return jsonObject;
        }
        JSONObject result=new JSONObject();
        jsonObject.put("status","0");
        jsonObject.put("msg","Success");
        String orderstr=getoids();
        result.put("order_str",orderstr);
        jsonObject.put("data",result);
        UpdateWrapper<Orders> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("oid",oid)
                .set("payment_platfrom",platform)
                .set("serial_number",orderstr)
                .set("state","已支付")
                .set("result","已支付成功");
        ordersMapper.update(null,updateWrapper);
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                "INFO","Orderpay "+oid+ "created successfully"));
        return jsonObject;
    }

    @PutMapping("/ride/confirm/{oid}")
    @ResponseBody
    public Object confirmPay(@PathVariable String oid,String uid,String platform,String trade_no){
        JSONObject jsonObject=new JSONObject();
        QueryWrapper<Orders> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("serial_number",trade_no);
        Orders orders=ordersMapper.selectOne(queryWrapper);
        if(orders!=null&&orders.getResult()!=null&&orders.getResult().equals("已支付成功")){
            jsonObject.put("status","0");
            jsonObject.put("msg","Success");
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                    "INFO","Orderpay "+oid+ "has been paid successfully"));
            return jsonObject;
        }
        jsonObject.put("status","error code");
        jsonObject.put("msg","Payment Failed");

        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"Order",
                "ERROR","Orderpay "+oid+ "has not been paid successfully"));
        return jsonObject;
    }
}
