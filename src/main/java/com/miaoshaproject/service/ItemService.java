package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    ItemModel creamItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    boolean decreeaseStock(Integer itemId,Integer amount)throws BusinessException;

    void increaseSales(Integer itemId,Integer amount) throws BusinessException;

}
