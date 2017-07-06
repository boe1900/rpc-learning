package com.bobo.rpc.sample;

import java.io.Serializable;

/**
 * Created by huabo on 2017/7/6.
 */
public class User implements Serializable{

    private static final long serialVersionUID = -2794554520184329354L;

    private String userId;

    private String userName;

    private int age;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
