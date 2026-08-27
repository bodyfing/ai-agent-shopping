package com.gooshare.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedVO {

    private Boolean liked;

    private Integer likeCount;
}
