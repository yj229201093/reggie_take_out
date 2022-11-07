package com.reggie.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.reggie_take_out.entity.OrderDetail;
import com.reggie.reggie_take_out.mapper.OrderDetailMapper;
import com.reggie.reggie_take_out.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
