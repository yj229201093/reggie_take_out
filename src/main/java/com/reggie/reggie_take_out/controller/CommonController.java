package com.reggie.reggie_take_out.controller;

import com.reggie.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {

        log.info(file.toString());
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        // 去后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 随机生成uuid,防止文件名称重复造成覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录
        File dir = new File(basePath);
        // 不存在创建文件夹
        if(!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 输入流 通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));
            // 输出流 通过输出流将文件写回浏览器，在浏览器展示图片了
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
