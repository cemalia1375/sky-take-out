package com.sky.service;

import com.sky.dto.ChatDTO;
import com.sky.vo.ChatSessionCreateVO;
import com.sky.vo.ChatSessionVO;
import com.sky.vo.ChatVO;

import java.util.List;

public interface ChatSessionService {

    /**
     * 创建新会话
     * @return
     */
    ChatSessionCreateVO createNewSession();

    /**
     * 获取会话列表
     * @param
     * @return
     */
    List<ChatSessionVO> getSessionList();

    /**
     * 发送信息
     * @param chatDTO
     * @return
     */
    ChatVO send(ChatDTO chatDTO);
}
