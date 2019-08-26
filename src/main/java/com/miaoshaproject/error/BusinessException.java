package com.miaoshaproject.error;

//包装器业务异常类实现
public class BusinessException extends Exception implements CommonError {

    private CommonError commonError;

    //直接接受EnbunissException的传参用于构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError=commonError;
    }

    //接受自定义Errormsg的方式构造业务异常
    public BusinessException(CommonError commonError,String errorMsg){
        super();
        this.commonError=commonError;
        this.commonError.setErrMsg(errorMsg);

    }

    @Override
    public int getErrorCode() {
        return this.commonError.getErrorCode();
    }

    @Override
    public String getErrorMsg() {
        return this.commonError.getErrorMsg();
    }

    @Override
    public CommonError setErrMsg(String errorMsg) {
        this.commonError.setErrMsg(errorMsg);

        return this;
    }
}
