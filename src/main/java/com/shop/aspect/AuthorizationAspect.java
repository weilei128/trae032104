package com.shop.aspect;

import com.shop.annotation.RequireRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Aspect
@Component
public class AuthorizationAspect {
    
    @Around("(@within(com.shop.annotation.RequireRole) || @annotation(com.shop.annotation.RequireRole))")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        
        RequireRole requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
        if (requireRole == null) {
            try {
                requireRole = (RequireRole) joinPoint.getSignature().getDeclaringType().getAnnotation(RequireRole.class);
            } catch (Exception ignored) {}
        }
        if (requireRole == null) {
            return joinPoint.proceed();
        }
        
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
        String username = (String) request.getAttribute("username");
        
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未登录\",\"code\":401}");
            return null;
        }
        
        if (requireRole.requireAdmin() && (isAdmin == null || !isAdmin)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"权限不足，需要管理员权限\",\"code\":403}");
            return null;
        }
        
        String[] roles = requireRole.value();
        if (roles.length > 0) {
            boolean hasRole = Arrays.asList(roles).contains(isAdmin != null && isAdmin ? "ADMIN" : "USER");
            if (!hasRole) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"权限不足\",\"code\":403}");
                return null;
            }
        }
        
        return joinPoint.proceed();
    }
}
