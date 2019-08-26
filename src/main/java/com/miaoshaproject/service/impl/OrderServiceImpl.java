package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusnissError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId,Integer amount) throws BusinessException {
        //入参校验
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw  new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel =userService.gerUserById(userId);
        if(userModel==null){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if(amount<=0||amount>99){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"数量信息不合法");
        }

        if(promoId!=null){
            if (promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else if(itemModel.getPromoModel().getStatus()!=2){
                throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
            }
        }
        //落单减库存
        boolean result = itemService.decreeaseStock(itemId,amount);
        if(!result){
            throw new BusinessException(EmBusnissError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel =new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());

        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrederPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生辰交易订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO =convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);
        itemService.increaseSales(orderDO.getItemId(),orderDO.getAmount());

        return orderModel;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)  //产生新事务，无论外部事件成功与否，都提交事务
    String generateOrderNo(){
        //时间信息+自增序列+分库分表位
        StringBuilder stringBuilder =new StringBuilder();
        LocalDateTime now =LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        int sequence=0;
        SequenceDO sequenceDO=sequenceDOMapper.getSequenceByName("order_info");
        sequence=sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr=String.valueOf(sequence);
        for (int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append("0");
        }
        stringBuilder.append(sequenceStr);


        stringBuilder.append("00");
        return stringBuilder.toString();
    }
    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);

        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrederPrice().doubleValue());
        return orderDO;

    }
}
