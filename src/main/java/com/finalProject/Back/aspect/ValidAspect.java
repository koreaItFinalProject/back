package com.finalProject.Back.aspect;

import com.finalProject.Back.dto.request.User.ReqOAuth2SignupDto;
import com.finalProject.Back.dto.request.User.ReqSignupDto;
import com.finalProject.Back.entity.OAuth2User;
import com.finalProject.Back.entity.User;
import com.finalProject.Back.exception.ValidException;
import com.finalProject.Back.repository.OAuth2UserMapper;
import com.finalProject.Back.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@Aspect
@Component
public class ValidAspect {
    private final UserService userService;
    private final OAuth2UserMapper oAuth2UserMapper;

    public ValidAspect(UserService userService , OAuth2UserMapper oAuth2UserMapper) {
        this.userService = userService;
        this.oAuth2UserMapper = oAuth2UserMapper;
    }

    @Pointcut("@annotation(com.finalProject.Back.aspect.annotation.ValidAop)")
    private void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        BeanPropertyBindingResult bindingResult = null;

        for(Object arg : args) {
            if(arg.getClass() == BeanPropertyBindingResult.class) {
                bindingResult = (BeanPropertyBindingResult) arg;
                break;
            }
        }

        switch (proceedingJoinPoint.getSignature().getName()){
            case "signup" :
                ValidSignupDto(args, bindingResult); // 주소만 가져옴
                break;
            case "oAuth2signup":
                ValidSignupDto(args , bindingResult);
                break;
            case "oAuth2nameDuplicate":
                oauth2NameDuplicate(args , bindingResult);
                break;
        }

        if(bindingResult.hasErrors()){
            throw new ValidException("유효성 검사 오류", bindingResult.getFieldErrors());
        }

        return proceedingJoinPoint.proceed();
    }
    
    // 공통 사용
    public void ValidSignupDto(Object[] args , BeanPropertyBindingResult bindingResult){
        for(Object arg : args){
            if(arg.getClass() == ReqSignupDto.class){
                ReqSignupDto dto = (ReqSignupDto) arg;

                if(!dto.getPassword().equals(dto.getCheckPassword())){
                    FieldError fieldError = new FieldError("checkPassword" , "checkPassword" , "유효하지 않은 비밀번호 입니다");
                    bindingResult.addError(fieldError);
                }
                if(userService.isDuplicateUsername(dto.getUsername())){
                    FieldError fieldError = new FieldError("username" , "username" , "이미 존재하는 아이디 입니다.");
                    bindingResult.addError(fieldError);
                }
            }
        }
    }

    public void ValidOAuth2SignupDto(Object[] args , BeanPropertyBindingResult bindingResult){
        for(Object arg : args){
            if(arg.getClass() == ReqOAuth2SignupDto.class){
                ReqOAuth2SignupDto dto = (ReqOAuth2SignupDto) arg;

                if(!dto.getPassword().equals(dto.getCheckPassword())){
                    FieldError fieldError = new FieldError("checkPassword" , "checkPassword" , "유효하지 않은 비밀번호 입니다");
                    bindingResult.addError(fieldError);
                }
                if(userService.isDuplicateUsername(dto.getUsername())){
                    FieldError fieldError = new FieldError("username" , "username" , "이미 존재하는 아이디 입니다.");
                    bindingResult.addError(fieldError);
                }
            }
        }
    }

    public void oauth2NameDuplicate(Object[] args, BeanPropertyBindingResult bindingResult){
        for(Object arg : args) {
            if (arg.getClass() == ReqOAuth2SignupDto.class) {

                ReqOAuth2SignupDto dto = (ReqOAuth2SignupDto) arg;

                if (oAuth2UserMapper.existsByOauth2Name(dto.getOauth2Name())) {
                    FieldError fieldError = new FieldError("oauth2Name", "oauth2Name", "이미 존재하는 OAuth2 이름입니다.");
                    bindingResult.addError(fieldError);
                }
            }
        }
    }
}
