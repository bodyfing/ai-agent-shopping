package com.gooshare.service.impl;

import com.gooshare.dto.IntentResult;
import com.gooshare.mapper.BrandMapper;
import com.gooshare.mapper.CategoryItemMapper;
import com.gooshare.mapper.CategoryMapper;
import com.gooshare.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {


    private final CategoryMapper categoryMapper;

    private final CategoryItemMapper categoryItemMapper;

    private final BrandMapper brandMapper;



    @Override
    public void convert(IntentResult result){


        // 品牌名称 -> 品牌ID
        if(result.getBrand()!=null){

            Long brandId =
                    brandMapper.selectIdByName(
                            result.getBrand()
                    );

            result.setBrandId(brandId);
        }



        // 一级分类名称 -> 分类ID
        if(result.getCategory()!=null){

            Long categoryId =
                    categoryMapper.selectIdByName(
                            result.getCategory()
                    );

            result.setCategoryId(categoryId);
        }



        // 二级分类名称 -> 二级分类ID
        if(result.getCategoryItem()!=null){

            Long itemId =
                    categoryItemMapper.selectIdByName(
                            result.getCategoryItem()
                    );

            result.setCategoryItemId(itemId);
        }

    }

}
