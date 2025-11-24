package com.application.sisacadepcc.config.security;

import com.application.sisacadepcc.service.AuthorizationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuthorizationAspect {

    private final AuthorizationService authorizationService;

    public AuthorizationAspect(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Around("@annotation(RequiresAdministratorAccess)")
    public Object checkAdministratorAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authorizationService.isAdministrator(authentication)) {
            throw new AccessDeniedException("Acceso denegado. Se requiere rol de administrador.");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(RequiresAdministratorOrSecretaryAccess)")
    public Object checkAdministratorOrSecretaryAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authorizationService.isAdministrator(authentication) && !authorizationService.isSecretary(authentication)) {
            throw new AccessDeniedException("Acceso denegado. Se requiere rol de administrador o secretaria.");
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(RequiresStudentAccess)")
    public Object checkStudentAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authorizationService.isStudent(authentication)) {
            throw new AccessDeniedException("Acceso denegado. Se requiere rol de estudiante.");
        }

        return joinPoint.proceed();
    }
}