package com.example.demo.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.JournalMapper;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceImp implements UserService{
    @Autowired
    private UserMapper mapper;
    @Autowired
    private JournalMapper journalMapper;
    @Override
    public boolean enroll(String phone,String identity,String car,
                          String car_num,String codes,String name,String uid,
                          String province,String city){
        mapper.insert(new User(uid,phone,identity,codes,name,car_num,car,0.0,province,city));
        return true;
    }
    @Override
    public User login(String phone,String token){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("phone",phone)
                    .eq("codes",token);
        User user=mapper.selectOne(queryWrapper);
        return user;
    }
    @Override
    public User uidSearch(String uid, String key){
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("uid",uid)
                .eq("codes",key);
        User user=mapper.selectOne(queryWrapper);
        return user;
    }

}
