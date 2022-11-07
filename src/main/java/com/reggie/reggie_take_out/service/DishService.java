package com.reggie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.reggie_take_out.dto.DishDto;
import com.reggie.reggie_take_out.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);
}
