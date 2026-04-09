package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Orders> getRecentOrders(Long userId) {
        // 简单写法：查最近5条
        return orderMapper.getRecentOrders(userId);
    }

    @Override
    public List<Orders> getUnfinishedOrders(Long userId) {
        return orderMapper.getUnfinishedOrders(userId);
    }

    @Override
    public Orders getByNumber(String number) {
        return orderMapper.getByNumber(number);
    }

    @Override
    public Map<String, Object> getOrderWithAddress(String number) {
        return orderMapper.getOrderWithAddress(number);
    }
}