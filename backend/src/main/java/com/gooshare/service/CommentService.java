package com.gooshare.service;

import com.gooshare.vo.CommentVO;

import java.util.List;

public interface CommentService {
    List<CommentVO> showComment(Long itemId);

    void addComment(Long itemId, String content);

    void addReply(Long itemId, Long commentId, String content);

    void deleteComment(Long commentId);
}
