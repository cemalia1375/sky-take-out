package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单分页查询,按下单时间排序
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据订单状态统计订单数量
     * @param deliveryInProgress
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer deliveryInProgress);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeTL(Integer status, LocalDateTime orderTime);

    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内的销量排名前10
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 小助手查询最近订单
     * @param userId
     * @return
     */
    @Select("select * from orders where user_id = #{userId} order by order_time desc limit 5")
    List<Orders> getRecentOrders(Long userId);

    /**
     * 小助手查询未完成订单
     * @param userId
     * @return
     */
    @Select("select * from orders where user_id = #{userId} and status in (1,2,3,4)")
    List<Orders> getUnfinishedOrders(Long userId);

    /**
     * 查询地址
     * @param number
     * @return
     */
    @Select("SELECT o.*, a.consignee, a.phone, a.detail " +
            "FROM orders o " +
            "LEFT JOIN address_book a ON o.address_book_id = a.id " +
            "WHERE o.number = #{number}")
    Map<String, Object> getOrderWithAddress(String number);
}
