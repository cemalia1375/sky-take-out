package com.sky.mapper;

import com.sky.entity.UserCoupon;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserCouponMapper {

    @Insert("insert into user_coupon(user_id, coupon_id, status, create_time) " +
            "values(#{userId}, #{couponId}, 0, now())")
    void insert(UserCoupon uc);

    @Select("SELECT c.*, uc.id AS ucId " +
            "FROM user_coupon uc " +
            "JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = 0")
    List<Map<String, Object>> getUserCoupons(Long userId);

    @Update("update user_coupon set status = 1, use_time = now() " +
            "where id = #{id} and status = 0")
    int markUsed(Long id);
}