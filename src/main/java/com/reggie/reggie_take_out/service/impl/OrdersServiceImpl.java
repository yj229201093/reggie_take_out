package com.reggie.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.reggie_take_out.common.BaseContext;
import com.reggie.reggie_take_out.common.CustomException;
import com.reggie.reggie_take_out.entity.*;
import com.reggie.reggie_take_out.mapper.OrdersMapper;
import com.reggie.reggie_take_out.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获得当前用户id
        Long userId = BaseContext.getId();

        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lambdaQueryWrapper);
        if(shoppingCartList == null || shoppingCartList.size() == 0)  {
            throw new CustomException("购物车为空，不能下单");
        }

        // 查询用户数据
        User user = userService.getById(userId);
        if (user == null) {
            throw new CustomException("用户信息为空");
        }

        // 查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        // 向订单表插入数据，一条数据
        long orderId = IdWorker.getId();

        // 总金额
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetailList = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(user.getPhone());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAddress(
                (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );

        this.save(orders);

        // 向明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetailList);

        // 清空购物车数据
        shoppingCartService.remove(lambdaQueryWrapper);
    }

    /**
     * 再来一单
     */
    @Override
    public void again(Orders orders) {
        // 从订单表 和 订单详情查出数据
        Orders ordersData = this.getById(orders.getId());

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(OrderDetail::getOrderId, orders.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);

        List<ShoppingCart> shoppingCarts = orderDetails.stream().map((item)->{
            // 添加到购物车
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(ordersData.getUserId());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());



        shoppingCartService.saveBatch(shoppingCarts);

    }
}
