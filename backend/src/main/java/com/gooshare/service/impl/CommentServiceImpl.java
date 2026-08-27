package com.gooshare.service.impl;

import static com.gooshare.common.RedisConstants.*;
import com.gooshare.mapper.CommentMapper;
import com.gooshare.service.CommentService;
import com.gooshare.utils.UserHolder;
import com.gooshare.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<CommentVO> showComment(Long itemId){
        Long userId = UserHolder.getUser().getId();
        List<CommentVO> comments = commentMapper.showCommentById(itemId);
        comments.forEach(comment -> {
            // 1. 先给父评论赋值
            Boolean isFollowed = stringRedisTemplate.opsForSet().isMember(FANS_PREFIX + comment.getUserId(), userId.toString());
            comment.setIsFollowed(Boolean.TRUE.equals(isFollowed));

            // 2. 判断是否有子评论，并遍历赋值
            if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
                comment.getChildren().forEach(child -> {
                    // 这里的 child 就是 CommentVO 类型了，可以调用 setIsFollowed
                    Boolean isChildFollowed = stringRedisTemplate.opsForSet().isMember(
                            FANS_PREFIX + child.getUserId(), // 注意这里要用 child 的 userId
                            userId.toString()
                    );
                    child.setIsFollowed(Boolean.TRUE.equals(isChildFollowed));
                });
            }
        });
        return comments;

    }

    @Override
    public void addComment(Long itemId, String content){
        Long userId = UserHolder.getUser().getId();
        String userName = UserHolder.getUser().getUsername();
        String userAvatar = UserHolder.getUser().getAvatar();
        Long role = UserHolder.getUser().getRole();
        Boolean isSeller = role == 1 ? true : false;
        commentMapper.addComment(itemId,userId,userName,userAvatar,content,isSeller);
    }

    @Override
    public void addReply(Long itemId, Long commentId, String content){
        Long userId = UserHolder.getUser().getId();
        Long role = UserHolder.getUser().getRole();
        String userName = UserHolder.getUser().getUsername();
        String userAvatar = UserHolder.getUser().getAvatar();
        LocalDateTime  now = LocalDateTime.now();
        Long parentId = commentId;
        Boolean isSeller = role == 1 ? true : false;
        commentMapper.addReply(itemId,userId,userName,userAvatar,now,parentId,isSeller,content);

    }
    @Override
    public void deleteComment(Long commentId){
        commentMapper.deleteById(commentId);
    }


}
