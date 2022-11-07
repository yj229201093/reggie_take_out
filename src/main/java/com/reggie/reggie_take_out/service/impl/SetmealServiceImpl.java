package com.reggie.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.reggie_take_out.common.CustomException;
import com.reggie.reggie_take_out.dto.SetmealDto;
import com.reggie.reggie_take_out.entity.Setmeal;
import com.reggie.reggie_take_out.entity.SetmealDish;
import com.reggie.reggie_take_out.mapper.SetmealMapper;
import com.reggie.reggie_take_out.service.SetmealDishService;
import com.reggie.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 插入数据到主表
        this.save(setmealDto);

        // 把套餐样品插入到套餐样品表
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        setmealDishList = setmealDto.getSetmealDishes().stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 先查询状态，是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0) {
            throw new CustomException("有套餐在售卖中，不可删除");
        }

        // 如果可以删除，先产出套餐表中的数据
        this.removeByIds(ids);

        // 删除关系表中的数据 setmeal_dish表中的数据
        LambdaQueryWrapper<SetmealDish> setmealDishQW = new LambdaQueryWrapper();
        setmealDishQW.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishQW);
    }

    @Override
    public void disableWithDish(int status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, status == 1 ? 0 : 1);
        List<Setmeal> list = this.list(queryWrapper);
        if(list.isEmpty()) {
            throw new CustomException("没有可处理的状态");
        }
        list = list.stream().map((item)->{
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());

        this.updateBatchById(list);
    }
}
