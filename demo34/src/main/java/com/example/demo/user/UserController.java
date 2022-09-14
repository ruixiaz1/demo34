package com.example.demo.user;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.journal.Journal;
import com.example.demo.mapper.JournalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController  {
    @Autowired
    private UserServiceImp userService;

    @Autowired
    private JournalMapper journalMapper;
    @PostMapping("/user")
    @ResponseBody
    public Object enroll(String phone,String identity,String car,
                         String car_num,String codes,String name,
                         String uid,String province,String city){
        Map<String,Object> m=new HashMap<>();
        if(phone.equals("") || codes.equals("") || identity.equals("")){
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                    "ERROR","New user "+name+" fail to enroll"));
            m.put("status","error code");
            m.put("msg","Role required");
            return m;
        }
        userService.enroll(phone,identity,car,car_num,codes,name,uid,province,city);
        m.put("status","0");
        m.put("msg","Success");
        journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                "INFO","New user "+name+"has enrolled in successfully"));
        return m;
    }
    @RequestMapping("/login")
    @ResponseBody
    public JSONObject login(String phone,String token){
        User user=userService.login(phone,token);
        JSONObject jsonobject=new JSONObject();
        if(user==null){
            jsonobject.put("status","error code");
            jsonobject.put("msg","User does not exist or password error");
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                    "ERROR","User "+phone+" does not exist or password error"));
        }
        else {
            jsonobject.put("status", "0");
            jsonobject.put("msg", "Success");
            jsonobject.put("data", JSONObject.toJSON(user));
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                    "INFO","User "+phone+"login successfully"));
        }
        return jsonobject;
    }
    @PutMapping("/user/{uid}")
    @ResponseBody
    public Object uidSearch(@PathVariable String uid, String key){
        User user=userService.uidSearch(uid,key);
        JSONObject jsonobject=new JSONObject();
        if(user==null){
            jsonobject.put("status","error code");
            jsonobject.put("msg","Invalid code");
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                    "ERROR","User "+uid+"enter error code"));
        }
        else {
            jsonobject.put("status", "0");
            jsonobject.put("msg", "Success");
            jsonobject.put("data", JSONObject.toJSON(user));
            journalMapper.insert(new Journal(new Timestamp(System.currentTimeMillis()),"User",
                    "INFO","User "+uid+"get information successfully"));
        }
        return jsonobject;
    }

}
