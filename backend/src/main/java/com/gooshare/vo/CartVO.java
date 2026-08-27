package com.gooshare.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartVO {
    private Integer itemId;

    private String title;

    private String imageURL;

    private Integer price;

    private Integer count;

    private boolean checked;
}
