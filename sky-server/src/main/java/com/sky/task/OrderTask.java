package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;
    /**
     *处理超时订单的方法
     */
    @Scheduled(cron = "0 * * * * ? ")//每分钟触发一次
    //@Scheduled(cron = "1/5 * * * * ?")//每五秒触发一次，测试用,从第一秒开始
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：｛｝", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status = ? and order_time < (当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeTL(Orders.PENDING_PAYMENT,time);

        if(ordersList !=null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直出于派送中状态的订单
     */
    @Scheduled(cron = "")//每天凌晨一点触发一次
    //@Scheduled(cron = "0/5 * * * * ?")//测试用
    public void proccessDeliveryOrder(){
        log.info("定时处理出于派送中的订单：｛｝",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        orderMapper.getByStatusAndOrderTimeTL(Orders.DELIVERY_IN_PROGRESS,time);

        //select * from orders where status = ? and order_time < (当前时间-15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeTL(Orders.PENDING_PAYMENT,time);

        if(ordersList !=null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
