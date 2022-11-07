package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.entity.Category;
import com.reggie.reggie_take_out.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 添加分类
     */
    @PostMapping
    public R<Category> save(@RequestBody  Category category) {
        categoryService.save(category);
        return R.success(category);
    }

    /**
     * 分页数据
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        log.info("page: {}, pageSize:{}", page, pageSize);
        // 创建分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 删除分类
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("删除分类 {}", ids);
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 修改
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        Category cat = categoryService.getById(category.getId());
        if(cat == null) {
            return R.error("分类不存在");
        }
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }


    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
