package com.reggie.reggie_take_out.controller;

import com.alibaba.druid.support.spring.stat.SpringStatUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.dto.SetmealDto;
import com.reggie.reggie_take_out.entity.Category;
import com.reggie.reggie_take_out.entity.Setmeal;
import com.reggie.reggie_take_out.service.CategoryService;
import com.reggie.reggie_take_out.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody  SetmealDto setmealDto) {
        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("添加套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page pageInfo = new Page<>(page, pageSize);
        Page setmealDtoPageInfo = new Page();
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
        // 条件查询
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        // 排序
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 查询
        setmealService.page(pageInfo, lambdaQueryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, setmealDtoPageInfo, "records");

        List<Setmeal> setmealList = pageInfo.getRecords();

        // 变量获取套餐分类
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            /// 查询分类名称
            Category category = categoryService.getById(item.getCategoryId());
            if(category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPageInfo.setRecords(setmealDtoList);

        return R.success(setmealDtoPageInfo);
    }


    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids == {}", ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }


    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("{}--{}", status,ids);
        setmealService.disableWithDish(status,ids);
        return R.success("设置成功");
    }

    @GetMapping("list")
    public R<List<Setmeal>> list (Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        lambdaQueryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(lambdaQueryWrapper);
        return R.success(setmealList);
    }

    @GetMapping("{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        return R.success(setmealDto);
    }

}
