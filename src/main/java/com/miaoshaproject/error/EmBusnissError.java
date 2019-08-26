package com.miaoshaproject.error;

public enum EmBusnissError implements CommonError {
    PARAMETER_VALIDATION_ERROR(100001,"参数不合法"),
    UNKNOW_ERROR(100002,"未知错误"),

    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAIL(20001,"用户手机号或密码不存在"),
    USER_NOT_LOGIN(20003,"用户未登陆"),

    STOCK_NOT_ENOUGH(30001,"库存不足"),




    ;

    private EmBusnissError(int errCode,String errMsg){
        this.errCode=errCode;
        this.errMsg=errMsg;
    }
    private int errCode;
    private  String errMsg;


    @Override
    public int getErrorCode() {
        return this.errCode;
    }

    @Override
    public String getErrorMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errorMsg) {
        this.errMsg=errorMsg;
        return this;
    }
}
