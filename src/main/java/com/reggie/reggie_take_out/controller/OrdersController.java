package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.reggie.reggie_take_out.common.BaseContext;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.entity.Orders;
import com.reggie.reggie_take_out.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：", orders);
        ordersService.submit(orders);
        return R.success("支付成功");
    }

    @GetMapping("page")
    public R<Page> page(int page,int pageSize, String number, String beginTime, String endTime) {
        log.info("page = {}, pageSize = {}, number = {number}, beginTime = {}, endTime = {}", page, pageSize, number, beginTime,endTime);
        Page pageInfo = new Page(page,pageSize);
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.orderByAsc(Orders::getOrderTime);
        lambdaQueryWrapper.eq(number != null ,Orders::getNumber, number);
        lambdaQueryWrapper.between((beginTime != null || endTime !=null), Orders::getCheckoutTime, beginTime, endTime);
        ordersService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        log.info("派送 {}", orders);
        orders = ordersService.getById(orders.getId());
        orders.setStatus(orders.getStatus() +1);
        ordersService.updateById(orders);
        return R.success("处理成功");
    }

    @GetMapping("userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getId();
        lambdaQueryWrapper.eq(Orders::getUserId, userId);
        lambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping("again")
    public R<String> again(@RequestBody Orders orders) {
        log.info("再来一单，{}", orders);
        ordersService.again(orders);
        return R.success("成功");
    }
}
