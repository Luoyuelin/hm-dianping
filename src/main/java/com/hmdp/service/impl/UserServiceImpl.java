package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Random;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
          return Result.fail("手机号格式错误");
        }
        //生成验证码
        String code= RandomUtil.randomNumbers(6);

        //保存验证码到seesion
        session.setAttribute("code",code);

        //发送验证码
        log.debug("验证码发送正确：验证码"+code);

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //不符合
            return Result.fail("手机号格式错误!");

        }

        //2.校验验证码
        Object seesCode = session.getAttribute("code");
        String code=loginForm.getCode();
        if(seesCode==null||!(seesCode.toString().equals(code))){
            return Result.fail("验证码错误");
        }
        //3.根据手机号进行查询
         User user = query().eq("phone", phone).one();
        //4.不存在则新建用户

        if (user==null){
            user=createUserWithPhone(phone);
        }
        //保存用户信息到session
        session.setAttribute("user",user);
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        //创建新的用户 手机号 昵称
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        //保存
        save(user);
        return user;
    }
}
