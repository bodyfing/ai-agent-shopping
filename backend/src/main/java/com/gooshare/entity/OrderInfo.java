package com.gooshare.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

    private Integer id;

    private Integer userId;

    private Integer itemId;

    private Integer sellerId;

    private Integer price;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime finishTime;
}
