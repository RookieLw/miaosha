package com.miaoshaproject.service.model;

import java.math.BigDecimal;

public class OrderModel {
    private String id;

    private Integer userId;

    private Integer itemId;

    //秒杀活动ID
    private Integer promoId;
    //秒杀时则为秒杀单价
    private BigDecimal itemPrice;


    private Integer amount;

    //秒杀时则为秒杀总价
    private BigDecimal orederPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrederPrice() {
        return orederPrice;
    }

    public void setOrederPrice(BigDecimal orederPrice) {
        this.orederPrice = orederPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
}
