package com.sky.service;

import com.sky.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {

    List<Coupon> listAvailable();

    void receive(Long couponId);

    Map<String, Object> selectBestCoupon(BigDecimal amount);
}