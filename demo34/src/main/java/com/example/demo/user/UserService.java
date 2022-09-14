package com.example.demo.user;

import org.springframework.stereotype.Service;

@Service
public interface UserService {

    boolean enroll(String phone,String identity,String car,
                   String car_num,String codes,String name,
                   String uid,String province,String city);
    User login(String phone,String token);
    User uidSearch(String uid,String key);
}
