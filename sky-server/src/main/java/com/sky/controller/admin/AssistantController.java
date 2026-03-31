package com.sky.controller.admin;

import com.sky.assistant.Assistant;
import com.sky.context.BaseContext;
import com.sky.dto.ChatDTO;
import com.sky.result.Result;
import com.sky.service.AiService;
import com.sky.service.ChatSessionService;
import com.sky.vo.ChatSessionCreateVO;
import com.sky.vo.ChatSessionVO;
import com.sky.vo.ChatVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/chat")
@Slf4j
@Api(tags="聊天助手接口")
public class AssistantController {

    @Autowired
    private Assistant assistant;
    @Autowired
    private AiService aiService;
    @Autowired
    private ChatSessionService chatSessionService;

    @PostMapping("/sessions")
    @ApiOperation("创建对话")
    public Result<ChatSessionCreateVO> creatSession() {
        log.info("创建对话");
        ChatSessionCreateVO newSession = chatSessionService.createNewSession();
        return Result.success(newSession);
    }

    @GetMapping("/sessions")
    @ApiOperation("获取当前管理员会话列表")
    public Result<List<ChatSessionVO>> list() {
        List<ChatSessionVO> list = chatSessionService.getSessionList();
        return Result.success(list);
    }

    /*
    @PostMapping("/assistant")
    @ApiOperation("发送消息")
    public Result<ChatVO> send(@RequestBody ChatDTO chatDTO){
        log.info("用户发送信息：{}",chatDTO);
        ChatVO chatVO = chatSessionService.send(chatDTO);
        return Result.success(chatVO);
    }*/

    /**
     * 流式发送消息
     * 注意：删掉了原来的同步 send 方法，避免冲突
     *
     * @param chatDTO
     * @return
     */
    @PostMapping("/assistant")
    @ApiOperation("发送消息（流式）")
    public SseEmitter send(@RequestBody ChatDTO chatDTO) {
        log.info("用户发起流式对话：{}", chatDTO);

        // 如果没有 sessionId，自动创建
        if (chatDTO.getSessionId() == null) {
            ChatSessionCreateVO session = chatSessionService.createNewSession();
            chatDTO.setSessionId(session.getSessionId());
        }
        return chatSessionService.send(chatDTO);
        /*
        // 1. 创建 SseEmitter，设置超时时间为 0（永不超时）
        SseEmitter emitter = new SseEmitter(0L);

        // 2. 先把用户的消息存入数据库（你可以根据项目逻辑决定是否在 Service 里存）
        //chatSessionService.saveUserMessage(chatDTO);

        // 3. 异步调用 AI 服务推送数据
        // 注意：这里的 memoryId 对应你前端传来的 sessionId
        aiService.streamGetReply(chatDTO.getSessionId(), chatDTO.getMessage(), emitter);

        return emitter;
        */

    }

    /**
     * 获取会话历史记录
     * @param sessionId
     * @return
     */
    @GetMapping("/history/{sessionId}")
    @ApiOperation("获取会话历史记录")
    public Result<ChatSessionVO> getHistory(@PathVariable String sessionId) {
        // 1. 处理前端误传的 "null" 字符串
        if (sessionId == null || "null".equals(sessionId)) {
            log.info("前端传参为 null，返回空列表");
            return Result.success(ChatSessionVO.builder().messages(new ArrayList<>()).build());
        }

        // 2. 正常查询逻辑
        try {
            Long id = Long.valueOf(sessionId);
            ChatSessionVO chatSessionVO = chatSessionService.getHistoryBySessionId(id);
            return Result.success(chatSessionVO); //
        } catch (NumberFormatException e) {
            return Result.error("会话ID格式错误");
        }
    }


}