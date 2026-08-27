package com.gooshare.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
public class UploadFile {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件
     * @param file 前端传来的文件对象
     * @return 文件的完整 URL
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 创建 OSS 客户端实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 2. 构造文件名 (防止重名覆盖)
            // 规则：2026/03/02/uuid.jpg
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = new DateTime().toString("yyyy/MM/dd") + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 上传文件流
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(bucketName, fileName, inputStream);

            // 4. 关闭客户端
            ossClient.shutdown();

            // 5. 返回访问路径
            // 格式：https://bucketName.endpoint/fileName
            return "https://" + bucketName + "." + endpoint + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}