package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.reggie.reggie_take_out.common.BaseContext;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.entity.ShoppingCart;
import com.reggie.reggie_take_out.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("进来了了了");
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);
        return R.success(shoppingCarts);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据：{}", shoppingCart);
        // 设置用户id, 指定当前是哪个用户的购物车数据
        Long userID = BaseContext.getId();
        shoppingCart.setUserId(userID);

        // 查询当前菜品或套餐是否存在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userID);
        if(dishId != null) {
            // 添加到购物车的是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 添加到购物车的是套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        // 如果已存在，就在原来的数据基础上加一
        // 如果不存在，添加到购物车，数量默认是一
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);
        if(cartServiceOne != null) {
            cartServiceOne.setNumber(cartServiceOne.getNumber()+1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    @PostMapping("sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long userID = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userID);
        if(shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart resultCart = shoppingCartService.getOne(queryWrapper);
        if(resultCart == null) {
            return R.error("商品不存在");
        }
        if(resultCart.getNumber() <= 0) {
            return R.error("已经没有可减了，加几个试试吧");
        }
        resultCart.setNumber(resultCart.getNumber()-1);
        shoppingCartService.updateById(resultCart);
        return R.success(resultCart);
    }

    @DeleteMapping("clean")
    public R<String> clean() {
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return R.success("清空购物车成功");
    }
}
