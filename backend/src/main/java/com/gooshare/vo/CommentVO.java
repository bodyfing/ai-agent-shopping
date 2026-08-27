package com.gooshare.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品留言展示对象
 * 适配“科研蓝”风格的前端渲染
 */
@Data
public class CommentVO implements Serializable {

    private Long id;

    private Long itemId;

    // --- 留言者信息 ---
    private Long userId;
    private String userName;
    private String userAvatar;

    // --- 留言内容 ---
    private String content;

    // --- 层级关系 ---
    private Long parentId; // 0代表一级留言

    // --- 被回复者信息 (用于显示：A 回复 B) ---
    private Long replyUserId;

    private String replyUserName;

    // --- 身份标识 ---
    private Boolean isSeller; // 是否为卖家本人，前端据此显示“楼主”蓝色标签

    // --- 时间 ---
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Boolean isFollowed;

    /**
     * 子留言列表 (楼中楼)
     * 支持多级回复的核心字段
     */
    private List<CommentVO> children;
}