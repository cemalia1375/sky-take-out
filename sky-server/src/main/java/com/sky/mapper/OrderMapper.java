package com.sky.mapper;

import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
}
