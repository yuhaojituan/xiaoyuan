package com.yuhaojituan.wemedia.filter;

import com.yuhaojituan.model.threadlocal.WmThreadLocalUtils;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Order(-1)
@WebFilter(filterName = "wmTokenFilter",urlPatterns = "/*")
@Slf4j
@Component  // 扫描包
public class WmTokenFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //得到header中的信息
        String userId = request.getHeader("userId");
        if(userId != null){
            WmUser wmUser = new WmUser();
            wmUser.setId(Integer.valueOf(userId));
            // 保存到当前线程中
            WmThreadLocalUtils.setUser(wmUser);
        }
        // 如果没有则直接放行
        filterChain.doFilter(request,response);
        // 过滤器处理完毕后  清空用户信息
        //todo  过滤器在service执行完了后还会执行下面的语句吗？？
        WmThreadLocalUtils.clear();
    }
}