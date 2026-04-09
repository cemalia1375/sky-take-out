package com.sky.controller.user;

import com.sky.entity.Coupon;
import com.sky.result.Result;
import com.sky.service.CouponService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/coupon")
@Api(tags = "C端-优惠券相关接口")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    public Result<List<Coupon>> list() {
        return Result.success(couponService.listAvailable());
    }

    @PostMapping("/receive/{id}")
    public Result receive(@PathVariable Long id) {
        couponService.receive(id);
        return Result.success();
    }
}