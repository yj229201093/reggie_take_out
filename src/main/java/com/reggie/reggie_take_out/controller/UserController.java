package com.reggie.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.reggie_take_out.common.R;
import com.reggie.reggie_take_out.entity.User;
import com.reggie.reggie_take_out.service.UserService;
import com.reggie.reggie_take_out.utils.SMSUtils;
import com.reggie.reggie_take_out.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private  UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        user.getPhone();
        // 获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)) {
            // 生产随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);
            // 调用阿里云提供的短信服务Api完成发送短信
//            SMSUtils.sendMessage("1","","","");

            // 需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success("发送验证成功");
        }

        return R.error("发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        // 获取手机号
        // 获取验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        // 从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        // 进行验证码的对比（页面提交的验证码和Session对比）
        if(codeInSession != null && codeInSession.equals(code)) {
            // 如果对比成功 说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if(user == null) {
                // 判断当前手机号对应的手机号是否为新用户，如果是新用户 自动注册完成

                R.success("自动注册");
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        log.info("退出成功");
        return R.success("退出成功");
    }

}
