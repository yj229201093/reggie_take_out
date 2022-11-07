package com.reggie.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.reggie_take_out.common.CustomException;
import com.reggie.reggie_take_out.dto.DishDto;
import com.reggie.reggie_take_out.entity.Dish;
import com.reggie.reggie_take_out.entity.DishFlavor;
import com.reggie.reggie_take_out.mapper.DishMapper;
import com.reggie.reggie_take_out.service.DishFlavorService;
import com.reggie.reggie_take_out.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品基本信息
        this.save(dishDto);

        // 菜品id
        Long dishId = dishDto.getId();
        // 菜品口味
        List<DishFlavor> flavorList = dishDto.getFlavors();
        flavorList = flavorList.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavorList);
    }

    @Override
    public DishDto getByWithFlavor(Long id) {
        // 查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        if(dish == null) {
            throw new CustomException("菜品信息不存在");
        }

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavorList = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavorList);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表基本信息
        this.updateById(dishDto);

        // 清理当前菜单对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper =  new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 添加当前提交过来的口味
        List<DishFlavor> flavorList = dishDto.getFlavors();
        flavorList = flavorList.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavorList);
    }
}
