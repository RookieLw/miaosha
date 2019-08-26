package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    //通过用户Id获取用户对象的方法

    UserModel gerUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;
    UserModel validaLogin(String telphone,String password) throws BusinessException;
}
