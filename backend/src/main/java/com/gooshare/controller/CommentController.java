package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.CommentService;
import com.gooshare.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论管理")
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Operation(summary = "获取商品评论列表")
    @GetMapping("/show/{itemId}")
    public Result showComment(@PathVariable Long itemId){
        List<CommentVO> comments = commentService.showComment(itemId);
        return Result.success(comments);
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result addComment(@RequestParam("itemId") Long itemId, @RequestParam("content") String content){
        commentService.addComment(itemId, content);
        return Result.success();
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    public Result addReply(@RequestParam("itemId") Long itemId,
                           @RequestParam("commentId") Long commentId,
                           @RequestParam("content") String content){
        commentService.addReply(itemId, commentId, content);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @PostMapping("/delete")
    public Result deleteComment(@RequestParam("commentId") Long commentId){
        commentService.deleteComment(commentId);
        return Result.success();
    }
}