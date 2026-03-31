package com.sky.dto;

import lombok.Data;

@Data
public class ChatDTO {
    private Long sessionId;   // 会话ID
    private String message;   // 用户消息内容
    private Long timestamp;   // 消息时间戳
}
