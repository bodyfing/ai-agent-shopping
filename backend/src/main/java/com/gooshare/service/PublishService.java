package com.gooshare.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gooshare.dto.UserDTO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.entity.Seller;

public interface PublishService extends IService<Seller> {
    void publish(ItemInfo itemInfo);
}
