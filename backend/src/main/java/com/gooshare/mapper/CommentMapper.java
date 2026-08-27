package com.gooshare.mapper;

import com.gooshare.vo.CommentVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentVO> showCommentById(Long itemId);

    void addComment(Long itemId, Long userId, String userName, String userAvatar, String content, Boolean isSeller);

    void addReply(Long itemId, Long userId, String userName, String userAvatar, LocalDateTime now, Long parentId, Boolean isSeller, String content);

    @Delete("DELETE FROM comment WHERE id = #{commentId}")
    void deleteById(Long commentId);
}
