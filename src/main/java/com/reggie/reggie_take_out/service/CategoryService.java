package com.reggie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.reggie_take_out.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
