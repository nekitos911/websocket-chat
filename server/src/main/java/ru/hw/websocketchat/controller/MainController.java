package ru.hw.websocketchat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.hw.websocketchat.service.ActiveUsersCountServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ActiveUsersCountServiceImpl activeUsersCountService;
    private final SimpUserRegistry simpUserRegistry;

//    @GetMapping("/")
//    public String index(HttpServletRequest request, Model model) {
//        String username = (String) request.getSession().getAttribute("username");
//
//        if (username == null || username.isEmpty()) {
//            return "redirect:/login";
//        }
//        model.addAttribute("username", username);
//
//        return "chat";
//    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping(value = {"/", "/index"})
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("usersCount", simpUserRegistry.getUserCount());
        return "/index";

//        messageService.readAll().stream().map(Message::getMesage)
//        String str = Stream.of(1, 2).map(s - s.toString())
//        .collect(Collectors.joining("<br>"));


    }


}
