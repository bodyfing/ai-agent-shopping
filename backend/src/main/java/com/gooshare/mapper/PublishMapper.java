package com.gooshare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gooshare.dto.UserDTO;
import com.gooshare.entity.ItemInfo;
import com.gooshare.entity.Seller;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PublishMapper extends BaseMapper<Seller> {
    Long publishItem(ItemInfo itemInfo);
}
