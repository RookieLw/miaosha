package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusnissError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.prefs.BackingStoreException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private  UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;
    @Override
    public UserModel gerUserById(Integer id) {
        //调用userdomapper获取到对应用户的dataobject
        UserDO userDO=userDOMapper.selectByPrimaryKey(id);

        if(userDO==null){
            return null;
        }
        UserPasswordDO userPasswordDO=userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);

    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//            ||userModel.getGender()==null
//            ||userModel.getAge()==null
//            ||StringUtils.isEmpty(userModel.getTelphone())){
//            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR);
//
//        }
        ValidationResult result=validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }



        //实现MODEl转成dataobject的方法

        UserDO userDO =convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"手机号已注册");
        }


        //将user插入后表后生成的id赋值给usermodel,转发给passworddo
        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO=convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;

        }

    @Override
    public UserModel validaLogin(String telphone, String password) throws BusinessException {
        //通过手机获取用户信息，比对密码
        UserDO userDO=userDOMapper.selectByTelphone(telphone);
        if(userDO==null){
            throw new BusinessException(EmBusnissError.USER_LOGIN_FAIL);
        }

        UserPasswordDO userPasswordDO=userPasswordDOMapper.selectByUserId(userDO.getId());

        UserModel userModel=convertFromDataObject(userDO,userPasswordDO);

        if(!StringUtils.equals(password,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusnissError.USER_LOGIN_FAIL);
        }
        return userModel;



    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO =new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());

        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }
    private UserDO convertFromModel(UserModel userModel) {

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);


        return userDO;
    }

    private  UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO==null){
            return null;
        }
        UserModel userModel=new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO!=null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
