package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusnissError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.impl.UserServiceImpl;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    //private UserService userService;
    private UserServiceImpl userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录服务
        UserModel userModel=userService.validaLogin(telphone,this.EncodeByMd5(password));
        //将登陆凭证加入到用户登录成功的session
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);

    }

    //用户注册接口
    @RequestMapping(value = "/register",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public  CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                      @RequestParam(name="otpCode")String otpCode,
                                      @RequestParam(name="name")String name,
                                      @RequestParam(name="gender")Integer gender,
                                      @RequestParam(name="age")Integer age,
                                      @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和OTPcode
        String inSessionOtpCode=(String) this.httpServletRequest.getSession().getAttribute(telphone);

        if(!StringUtils.equals(inSessionOtpCode,otpCode)){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"短信验证码不正确");
        }


        //用户注册流程
        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("by phone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userModel.setThirdPartyId("wechat");

        userService.register(userModel);
        return CommonReturnType.create(null);

    }

    public String EncodeByMd5(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //计算方法
        MessageDigest md5 =MessageDigest.getInstance("Md5");
        BASE64Encoder base64Encoder =new BASE64Encoder();
        //加密
        String newstr =base64Encoder.encode(md5.digest(string.getBytes("utf-8")));
        return newstr;

    }


    //用户获取OTP短信接口
    @RequestMapping(value = "/getotp",method={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //1按照一定规则获取OTP验证码
        Random random=new Random();
        int randomInt =random.nextInt(99999);
        randomInt+=10000;
        String otpCode =String.valueOf(randomInt);

        //2将验证码与手机号关联
        httpServletRequest.getSession().setAttribute(telphone,otpCode);



        //3短信发送
        System.out.println("telphone="+telphone+"&otpCode="+otpCode);


        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //调用service服务获取对应的id的用户对象并返回给前端
        UserModel userModel=userService.gerUserById(id);

        //若获取的用户信息不存在
        if(userModel == null){

            throw new BusinessException(EmBusnissError.USER_NOT_EXIST);
        }

        //将核心领域模型转化成可供UI端使用的viewobject
        UserVO userVO=convertFromModel(userModel);


        return CommonReturnType.create(userVO);
    }

    private  UserVO convertFromModel(UserModel userModel){
        if(userModel==null){
            return null;

        }
        UserVO userVO =new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

    //定义EXCEPTIONHANDLER解决未被controller层吸收的exception


}
