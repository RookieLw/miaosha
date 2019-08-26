package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusnissError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private PromoService promoService;

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO =new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO=new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel=new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);

        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }



    @Override
    public ItemModel creamItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result =validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusnissError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        //itemmodel-》daaobject
        ItemDO itemDO=this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());


        ItemStockDO itemStockDO=this.convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);




        //返回创建完成的对象

        return this.getItemById(itemModel.getId());



    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList =itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO =itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel=convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {

        ItemDO itemDO =itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null){
            return null;

        }
        ItemStockDO itemStockDO=itemStockDOMapper.selectByItemId(itemDO.getId());



        ItemModel itemModel=convertModelFromDataObject(itemDO,itemStockDO);


        //获取活动信息
        PromoModel promoModel = promoService.getPromoById(itemModel.getId());
        if(promoModel!=null && promoModel.getStatus()!=3){
            itemModel.setPromoModel(promoModel);
        }


        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreeaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affrctedRow = itemStockDOMapper.decreaseStock(itemId,amount);
        if(affrctedRow>0){
            return true;
        }else {
            return false;
        }


    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }


}
