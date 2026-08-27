package com.gooshare.service;

import com.gooshare.common.Result;

public interface FollowService {
    Result follow(Long followId, Boolean isFollowed, Long type);

    Result getCommonFollowers(Long sellerId);
}
