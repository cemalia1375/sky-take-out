package com.sky.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCoupon {
    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime useTime;
}