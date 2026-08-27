package com.gooshare.controller;

import com.gooshare.common.Result;
import com.gooshare.service.PublishService;
import com.gooshare.utils.UploadFile;
import com.gooshare.entity.ItemInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "发布管理", description = "处理商品图片上传及信息发布")
@RequestMapping("/publish")
@RestController
public class PublishController {

    @Autowired
    private PublishService publishService;
    @Autowired
    private UploadFile uploadFile;

    @Operation(summary = "上传商品图片")
    @PostMapping("/image")
    public Result image(@RequestParam("file") MultipartFile imageFile){
        String imageURL = uploadFile.uploadFile(imageFile);
        System.out.println(imageURL+"图片！！！！！");
        return Result.success(imageURL);
    }

    @Operation(summary = "提交发布商品")
    @PostMapping
    public Result publish(@RequestBody ItemInfo itemInfo){
        publishService.publish(itemInfo);
        return Result.success();
    }
}