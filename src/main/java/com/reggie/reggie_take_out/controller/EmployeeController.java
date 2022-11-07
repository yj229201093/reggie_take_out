package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.entity.Employee;
import com.reggie.reggie_take_out.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1、将页面提交的密码password 进行加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        // 如果没有查询到则返回登录失败结果
        if(emp == null) {
            return R.error("用户不存在");
        }

        // 密码对比
        if(!emp.getPassword().equals(password)) {
            return R.error("密码不正常");
        }

        // 查看员工状态
        if(emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        log.info("退出成功");
        return R.success("退出成功");
    }

    /**
     * 添加成员
     */
    @PostMapping()
    public R<String> add(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工信息");
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        long id = Thread.currentThread().getId();
        log.info("线程id: {}", id);
        employee.setStatus(1);
        employeeService.save(employee);
        return R.success("添加成功");
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 先创建MybatisPlusConfig 配置文件
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        // 构造条件构造
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee) {
//         Long empId = (Long) request.getSession().getAttribute("employee");
//         employee.setUpdateTime(LocalDateTime.now());
//         employee.setUpdateUser(empId);
        long id = Thread.currentThread().getId();
        log.info("线程id: {}", id);
         employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据{}查询员工信息",id);
        Employee employee = employeeService.getById(id);

        if(employee == null) {
            return R.error("用户不存在");
        }
        return R.success(employee);
    }
}
