package com.reggie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.reggie_take_out.dto.SetmealDto;
import com.reggie.reggie_take_out.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithSetmealDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);

    public void disableWithDish(int status,List<Long> ids);
}
