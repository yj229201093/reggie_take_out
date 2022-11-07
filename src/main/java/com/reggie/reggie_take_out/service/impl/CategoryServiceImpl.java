package com.reggie.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.reggie_take_out.common.CustomException;
import com.reggie.reggie_take_out.entity.Category;
import com.reggie.reggie_take_out.entity.Dish;
import com.reggie.reggie_take_out.entity.Setmeal;
import com.reggie.reggie_take_out.mapper.CategoryMapper;
import com.reggie.reggie_take_out.service.CategoryService;
import com.reggie.reggie_take_out.service.DishService;
import com.reggie.reggie_take_out.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private DishService dishService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     */
    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联了菜品，如果已经关联，不可删
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(queryWrapper);
        if(dishCount > 0) {
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查询当前分类是否关联了套餐，如果已经关联，不可删
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper();
        qw.eq(Setmeal::getCategoryId,id);
        int setmealCount = setmealService.count(qw);
        if(setmealCount > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 正常删除分类
        super.removeById(id);
    }
}
