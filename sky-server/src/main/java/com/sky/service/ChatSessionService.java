package com.sky.service;

import com.sky.dto.ChatDTO;
import com.sky.vo.ChatSessionCreateVO;
import com.sky.vo.ChatSessionVO;
import com.sky.vo.ChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    SseEmitter send(ChatDTO chatDTO);

    /**
     * 获取会话历史
     * @param sessionId
     * @return
     */
    ChatSessionVO getHistoryBySessionId(Long sessionId);


}
