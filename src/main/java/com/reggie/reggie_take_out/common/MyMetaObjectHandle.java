package com.reggie.reggie_take_out.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandle implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充【insert】");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getId());
        metaObject.setValue("updateUser", BaseContext.getId());
        log.info(metaObject.toString());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充【update】");
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getId());
        log.info(metaObject.toString());
    }
}
