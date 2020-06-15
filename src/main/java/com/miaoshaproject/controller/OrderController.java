package com.miaoshaproject.controller;


import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusnissError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    //封装下单请求
    @RequestMapping(value = "/createorder",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        //@RequestParam(name = "userId")Integer userId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId",required = false)Integer promoId) throws BusinessException {
        //获取登录用户信息
        Boolean isLogin= (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin==null||!isLogin.booleanValue()){
            throw new BusinessException(EmBusnissError.USER_NOT_LOGIN,"用户还未登录，不能下单");
        }
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");


        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);

        return CommonReturnType.create(null);
    }
}