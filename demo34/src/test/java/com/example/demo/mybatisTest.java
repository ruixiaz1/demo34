package com.example.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo.distribution.Ride;
import com.example.demo.mapper.RideMapper;
import com.example.demo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;

@SpringBootTest
public class mybatisTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RideMapper rideMapper;

//    @Test
//    public void testSelectList(){
//        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
//        queryWrapper.eq("phone","15995675450")
//                .eq("codes","aptx486");
//        User user=userMapper.selectOne(queryWrapper);
//        System.out.println(user);
//
//    }
    @Test
    public void testupdate(){
        UpdateWrapper<Ride> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("rid","36mHdgqy").set("state","接单");
        rideMapper.update(null,updateWrapper);
        System.out.println(rideMapper.selectOne(new QueryWrapper<Ride>().eq("rid","36mHdgqy")));

    }
    @Test
    public void testTimeUpdate(){
        Timestamp t=new Timestamp(System.currentTimeMillis());
        UpdateWrapper<Ride> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("rid","36mHdgqy").set("ctime",t);
        rideMapper.update(null,updateWrapper);
        System.out.println(rideMapper.selectOne(new QueryWrapper<Ride>().eq("rid","36mHdgqy")));
    }

}
