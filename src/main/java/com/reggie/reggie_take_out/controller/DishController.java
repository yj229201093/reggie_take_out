package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.dto.DishDto;
import com.reggie.reggie_take_out.entity.Category;
import com.reggie.reggie_take_out.entity.Dish;
import com.reggie.reggie_take_out.entity.DishFlavor;
import com.reggie.reggie_take_out.service.CategoryService;
import com.reggie.reggie_take_out.service.DishFlavorService;
import com.reggie.reggie_take_out.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService flavorService;

    /**
     * 新增菜品
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto) {
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name) {
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝 忽略records 字段
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        // 遍历数据，拿id查名称
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId =  item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("修改");
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 根据菜品分量查询菜品名称列表
     */
    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort);
        List<Dish> dishList = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = dishList.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> qw = new LambdaQueryWrapper<>();
            qw.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> list = flavorService.list(qw);
            dishDto.setFlavors(list);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }

    /**
     * 停售，启售，（批量停售，批量启售）
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("{}--{}", status,ids);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(Dish::getId, ids);
        lambdaQueryWrapper.eq(Dish::getStatus, status == 0 ? 1: 0);
        List<Dish> dishList = dishService.list(lambdaQueryWrapper);
        if(dishList == null || dishList.size() == 0) {
            return R.error("没有可操作的数据");
        }
        dishList = dishList.stream().map((item)-> {
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        dishService.updateBatchById(dishList);
        return R.success("设置成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids = {}", ids);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(Dish::getId, ids);
        dishService.remove(lambdaQueryWrapper);
        return R.success("删除成功");
    }
}
