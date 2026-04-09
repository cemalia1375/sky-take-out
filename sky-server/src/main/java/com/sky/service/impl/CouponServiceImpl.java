package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.Coupon;
import com.sky.entity.UserCoupon;
import com.sky.mapper.CouponMapper;
import com.sky.mapper.UserCouponMapper;
import com.sky.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    public List<Coupon> listAvailable() {
        return couponMapper.listAll();
    }

    @Override
    public void receive(Long couponId) {
        Long userId = BaseContext.getCurrentId();

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);

        userCouponMapper.insert(uc);
    }

    /**
     * 🔥 自动选择最优优惠券
     */
    @Override
    public Map<String, Object> selectBestCoupon(BigDecimal amount) {

        Long userId = BaseContext.getCurrentId();
        List<Map<String, Object>> list = userCouponMapper.getUserCoupons(userId);

        BigDecimal maxDiscount = BigDecimal.ZERO;
        Map<String, Object> best = null;

        for (Map<String, Object> c : list) {

            Integer type = (Integer) c.get("type");
            BigDecimal condition = (BigDecimal) c.get("condition_amount");

            if (amount.compareTo(condition) < 0) continue;

            BigDecimal discount = BigDecimal.ZERO;

            if (type == 1) {
                discount = (BigDecimal) c.get("discount_amount");
            } else if (type == 2) {
                BigDecimal rate = (BigDecimal) c.get("discount_rate");
                discount = amount.multiply(BigDecimal.ONE.subtract(rate));
            }

            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                best = c;
            }
        }

        if (best != null) {
            best.put("discount", maxDiscount);
        }

        return best;
    }
}