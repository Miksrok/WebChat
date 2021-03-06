package com.github.mykhalechko.webchat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mykhalechko.webchat.dto.ChatUserDto;
import com.github.mykhalechko.webchat.entity.ChatUser;
import com.github.mykhalechko.webchat.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

@RestController
public class LoginController {

    @Autowired
    LoginService service;

    @RequestMapping(value = "/login", name = "getLogin", method = RequestMethod.GET)
    public ModelAndView getLogin() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("urlpath", "/getLoginLink");
        modelAndView.setViewName("login");
        return modelAndView;
    }

    //use spring-hateoas
    //получение ссылки для проверки пароля
    @RequestMapping(value = "/getLoginLink", method = RequestMethod.GET)
    public ChatUserDto getChatUserLinkForSendingCredentials() throws NoSuchMethodException, JsonProcessingException {
        Method verifyLoginMethod = LoginController.class.getMethod("verifyLogin", ChatUserDto.class, HttpSession.class);
        Link verifyLoginLink = ControllerLinkBuilder.linkTo(LoginController.class, verifyLoginMethod).withSelfRel();
        System.out.println(verifyLoginLink.getHref());
        ChatUserDto chatUser = new ChatUserDto();
        chatUser.add(verifyLoginLink);
        return chatUser;
    }

    //проверка логина и пароля
    @RequestMapping(value = "/verifyLogin", method = RequestMethod.POST)
    public ResourceSupport verifyLogin(@RequestBody ChatUserDto chatUserDto, HttpSession session) throws NoSuchMethodException {
        System.out.println("Before");
        System.out.println(chatUserDto.toString());
        if (service.verifyLogin(chatUserDto)) {
            System.out.println("After");
            session.setAttribute("login", chatUserDto.getLogin());
            Method chatMethod = ChatController.class.getMethod("chatAccess", HttpSession.class);
            Link chatinLink = ControllerLinkBuilder.linkTo(ChatController.class, chatMethod).withSelfRel();
            ResourceSupport link = new ResourceSupport();
            link.add(chatinLink);
            return link;
        } else {
            Method chatMethod = ErrorController.class.getMethod("getError");
            Link chatinLink = ControllerLinkBuilder.linkTo(ErrorController.class, chatMethod).withSelfRel();
            ResourceSupport link = new ResourceSupport();
            link.add(chatinLink);
            return link;
        }
    }
}
