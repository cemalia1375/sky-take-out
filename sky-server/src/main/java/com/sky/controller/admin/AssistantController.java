package com.sky.controller.admin;

import com.sky.assistant.Assistant;
import com.sky.context.BaseContext;
import com.sky.dto.ChatDTO;
import com.sky.result.Result;
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

import java.util.List;

@RestController
@RequestMapping("/admin/chat")
@Slf4j
@Api(tags="聊天助手接口")
public class AssistantController {

    @Autowired
    private Assistant assistant;

    @Autowired
    private ChatSessionService chatSessionService;

    @PostMapping("/session")
    @ApiOperation("创建对话")
    public Result<ChatSessionCreateVO> creatSession(){
        log.info("创建对话");
        ChatSessionCreateVO newSession = chatSessionService.createNewSession();
        return Result.success(newSession);
    }

    @GetMapping("/session/list")
    @ApiOperation("获取当前管理员会话列表")
    public Result<List<ChatSessionVO>> list() {
        List<ChatSessionVO> list = chatSessionService.getSessionList();
        return Result.success(list);
    }

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<ChatVO> send(@RequestBody ChatDTO chatDTO){
        log.info("用户发送信息：{}",chatDTO);
        ChatVO chatVO = chatSessionService.send(chatDTO);
        return Result.success(chatVO);
    }
}
