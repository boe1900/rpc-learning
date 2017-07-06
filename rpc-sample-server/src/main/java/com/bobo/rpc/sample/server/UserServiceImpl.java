package com.bobo.rpc.sample.server;

import com.bobo.rpc.sample.User;
import com.bobo.rpc.sample.UserService;
import com.bobo.rpc.server.RpcService;

/**
 * Created by huabo on 2017/7/6.
 */
@RpcService(UserService.class)
public class UserServiceImpl implements UserService{

    public User getUserById(String userId) {
        User user = new User();
        user.setUserId("1");
        user.setUserName("bobo");
        user.setAge(25);
        return user;
    }
}
