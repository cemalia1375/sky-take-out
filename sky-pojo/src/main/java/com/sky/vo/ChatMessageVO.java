package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    private String role;      // 角色：user | assistant
    private String content;   // 消息内容
    private Long timestamp;   // 13位时间戳
}
