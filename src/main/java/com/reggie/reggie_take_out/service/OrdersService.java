package com.reggie.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.reggie_take_out.entity.Orders;

import java.util.Objects;

public interface OrdersService extends IService<Orders> {

    public void submit(Orders orders);

    public void again(Orders orders);
}
