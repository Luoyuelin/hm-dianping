package com.hmdp.utils;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author：lyl
 * @Package：com.hmdp.utils
 * @Project：hm-dianping
 * @name：LoginInterceptor
 * @Date：2025/4/22 21:09
 * @Filename：LoginInterceptor
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.从session中取出user
        User user=(User)request.getSession().getAttribute("user");

        //2.判断user是否存在
        if(user==null){
            //401未授权
            response.setStatus(401);
            return false;
        }

        //3.保存到ThreadLocal
        UserHolder.saveUser(user);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
