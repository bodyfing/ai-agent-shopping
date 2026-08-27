package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.InboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息推送管理")
@RestController
@RequestMapping("/inbox")
public class InboxController {

    @Autowired
    private InboxService inboxService;

    @Operation(summary = "获取收件箱列表")
    @PostMapping
    public Result getInbox(@RequestParam("max") Long max, @RequestParam("offset") Integer offset){
        return inboxService.getInbox(max, offset);
    }
}