package com.example.test.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.ws.RequestWrapper;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    @RequestMapping("/user")
    @ResponseBody
    public Map<String,String> enroll(int phone, String code, String name){
        Map<String,String> m=new HashMap<>();
        m.put("status","0");
        m.put("msg","Success");
        return m;
    }
}
