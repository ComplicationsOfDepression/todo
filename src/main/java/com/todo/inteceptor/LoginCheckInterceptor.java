package com.todo.inteceptor;

import com.todo.common.Result;
import com.todo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        String url=request.getRequestURL().toString();
        log.info("请求的url:{}", url);
        response.setContentType("text/html;charset=UTF-8");
        if (url.contains("login")){
            log.info("登录放行");
            return true;
        }
        if (url.contains("register")){
            log.info("注册放行");
            return true;
        }

        String jwt=request.getHeader("Token");

        // 如果token为空，返回未登录信息
        if (!StringUtils.hasLength(jwt)) {
            Result error = Result.login();
            String notLogin = com.alibaba.fastjson.JSONObject.toJSONString(error);
            response.getWriter().write(notLogin);
            return false;
        }


        // 解析令牌，检查是否合法
        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            if(claims == null){
                Result error = Result.login();
                String notLogin = com.alibaba.fastjson.JSONObject.toJSONString(error);
                response.getWriter().write(notLogin);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败");
            return false;
        }

        log.info("令牌合法，放行");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
        // 在请求处理之后但视图渲染之前调用
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 在整个请求完成之后，即视图渲染结束之后调用
        log.info("afterCompletion");
    }
}
