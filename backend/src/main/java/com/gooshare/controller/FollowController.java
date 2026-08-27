package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "社交关注管理")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Operation(summary = "关注或取消关注")
    @PostMapping("/add")
    public Result follow(@RequestParam("followId") Long followId,
                         @RequestParam("isFollowed") Boolean isFollowed,
                         @RequestParam("type") Long type) throws Exception {
        return followService.follow(followId, isFollowed, type);
    }

    @Operation(summary = "获取共同关注列表")
    @PostMapping("/common")
    public Result getCommonFollowers(@RequestParam("sellerId") Long sellerId) {
        return followService.getCommonFollowers(sellerId);
    }
}