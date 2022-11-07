package com.reggie.reggie_take_out.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.reggie_take_out.common.BaseContext;
import com.reggie.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路劲匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        log.info("拦截到请求：{}", request.getRequestURI());
        // 获取本次请求的URL
        String requestRI = request.getRequestURI();
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"

        };

        // 判断本次请求是否需要处理
        boolean check = check(urls, requestRI);

        /// 判断
        if(check) {
            log.info("本次请求{}不需要处理",requestRI);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1 判断登录状态
        if(request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            long id = Thread.currentThread().getId();
            log.info("线程id: {}", id);
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        // 4-1 判断登录状态
        if(request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            Long empId = (Long) request.getSession().getAttribute("user");
            long id = Thread.currentThread().getId();
            log.info("线程id: {}", id);
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }

        log.info("未登录",requestRI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
//        filterChain.doFilter(request,response);
    }


    /**
     * 路劲匹配
     */
    public boolean check(String[] urls,String requestURI) {
        for (String url:urls) {
            if(PATH_MATCHER.match(url,requestURI)) {
                return true;
            }
        }
        return false;
    }
}
