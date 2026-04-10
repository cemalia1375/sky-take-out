package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderDelayListener {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderServiceImpl orderService; // 这里为了复用rollback逻辑

    @PostConstruct
    public void startListener() {
        new Thread(() -> {
            RBlockingQueue<Long> blockingQueue = redissonClient.getBlockingQueue("orderDelayQueue");
            while (!redissonClient.isShutdown()) {
                try {
//                    // 阻塞式获取到期的订单ID
//                    Long orderId = blockingQueue.take();

                    // 建议使用带超时时间的 poll，而不是死等 take
                    // 这样线程可以定期醒来检查 isShutdown 状态
                    Long orderId = blockingQueue.poll(5, TimeUnit.SECONDS);

                    if (orderId == null) continue; // 没拿到数据，继续下一轮循环
                    log.info("延迟队列检测到待处理订单ID: {}", orderId);

                    Orders ordersDB = orderMapper.getById(orderId);

                    // 只有状态为“待付款”的订单才执行取消
                    if (ordersDB != null && ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)) {
                        log.info("订单 {} 支付超时，正在执行自动取消...", orderId);

                        // 1. 修改数据库状态（建议在Service里写个专门的autoCancel方法以复用事务）
                        // 这里直接演示逻辑：
                        Orders updateOrder = Orders.builder()
                                .id(orderId)
                                .status(Orders.CANCELLED)
                                .cancelReason("订单支付超时，系统自动取消")
                                .build();
                        orderMapper.update(updateOrder);

                        // 2. 释放资源（调用刚才封装的方法）
                        // 注意：此处需要将 rollbackResources 权限改为 public 或通过反射调用
//                        Method method = OrderServiceImpl.class.getDeclaredMethod("rollbackResources", Long.class);
//                        method.setAccessible(true);
//                        method.invoke(orderService, orderId);

                        // 直接一行搞定，就像调用普通方法一样
                        orderService.rollbackResources(orderId);

                        log.info("订单 {} 自动取消成功并释放库存", orderId);
                    }
                } catch (InterruptedException e) {
                    log.info("延迟队列监听线程被中断，正在退出...");
                    break;
                } catch (RedissonShutdownException e) {
                    log.warn("Redisson已关闭，延迟监听停止。");
                    break;
                } catch (Exception e) {
                    log.error("处理延迟取消任务异常", e);
                }
            }
        }, "Order-Delay-Queue-Thread").start();
    }
}