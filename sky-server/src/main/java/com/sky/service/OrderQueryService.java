package com.sky.service;

import com.sky.entity.Orders;

import java.util.List;

public interface OrderQueryService {
    // 最近订单
    List<Orders> getRecentOrders(Long userId);

    // 未完成订单
    List<Orders> getUnfinishedOrders(Long userId);

    // 根据订单号查询
    Orders getByNumber(String number);
}
