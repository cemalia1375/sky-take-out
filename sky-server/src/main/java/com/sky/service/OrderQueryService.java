package com.sky.service;

import com.sky.entity.Orders;

import java.util.List;
import java.util.Map;

public interface OrderQueryService {
    // 最近订单
    List<Orders> getRecentOrders(Long userId);

    // 未完成订单
    List<Orders> getUnfinishedOrders(Long userId);

    // 根据订单号查询
    Orders getByNumber(String number);

    // 查询地址
    Map<String, Object> getOrderWithAddress(String number);
}
