package com.project.milo.controller;

import com.google.code.kaptcha.Constants;
import com.project.milo.common.AjaxResult;
import com.project.milo.entity.User;
import com.project.milo.utils.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("")
public class LoginController {


    @GetMapping("login")
    public String login(){
        return "login/login";
    }

    /**
     * 判断登录用户名、密码、验证码是否正确
     * @param request
     * @param userPwd
     * @param userCode
     * @param captcha
     * @return
     */
    @PostMapping("login_check")
    @ResponseBody
    public AjaxResult ajaxLogin(HttpServletRequest request, String userPwd, String userCode, String captcha) {

        //User user = iUserService.getByUserCodeAndUserName(userPwd,userCode);
        User user = new User();
        user.setUserCode("1101");
        user.setUserPwd("123.");
        user.setUserName("张三");

        HttpSession session = request.getSession();
        String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (StringUtils.isNotNull(user)){
            if (StringUtils.isNotEmpty(user.getUserCode()) && StringUtils.isNotEmpty(user.getUserPwd())){
                request.getSession().setAttribute("userName", user.getUserName());
                if(userCode.equals(user.getUserCode()) && userPwd.equals(user.getUserPwd())){
                    if(captcha.equals(code)){
                        return AjaxResult.success("登录成功");
                    }else {
                        return AjaxResult.error("验证码错误！");
                    }
                }else {
                    return AjaxResult.error("用户名或密码错误！");
                }

            }
        }
        return AjaxResult.error("用户不存在！");
    }


    @GetMapping("index")
    public String index(){

        return "index/index";
    }

    /*@GetMapping("/index")
    public String index(HttpServletRequest request) {
        // 如果请求包含任何参数，重定向到无参数的 /index
        if (!request.getParameterMap().isEmpty()) {
            return "redirect:/index";
        }
        return "index";
    }*/
}
