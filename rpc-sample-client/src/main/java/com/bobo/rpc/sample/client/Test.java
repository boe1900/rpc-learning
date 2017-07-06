package com.bobo.rpc.sample.client;

import com.bobo.rpc.client.RpcProxy;
import com.bobo.rpc.sample.User;
import com.bobo.rpc.sample.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by huabo on 2017/7/6.
 */
public class Test {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);
        UserService userService = rpcProxy.create(UserService.class);
        User user = userService.getUserById("1");
        System.out.println(user.getUserName());

        System.exit(0);
    }
}
