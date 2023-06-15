package com.codeup.plantapp.controllers;

import com.codeup.plantapp.models.Friend;
import com.codeup.plantapp.models.User;
import com.codeup.plantapp.repositories.FriendRepository;
import com.codeup.plantapp.repositories.UserRepository;
import com.codeup.plantapp.services.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/friends")
public class FriendsController {

        private final UserRepository usersDao;
        private final FriendRepository friendDao;

        public FriendsController(UserRepository usersDao, FriendRepository friendDao){
            this.usersDao = usersDao;
            this.friendDao = friendDao;
        }

        @Autowired
        private Keys keys;

    @GetMapping("/")
    public String allUsers(Model model) {
        List<User> users  = usersDao.findAll();
        model.addAttribute("users", users);
        return "friends";
    }


    @GetMapping("/{id}")
    public String addFriend(@PathVariable long id,
                            RedirectAttributes redirectAttributes){
        User user1 = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = usersDao.findUserById(id);

        Friend newRequest = new Friend(user1, user, false);
        friendDao.save(newRequest);

        redirectAttributes.addFlashAttribute("successMessage", "Comment submitted successfully!");

        return "redirect:/friends/";
    }

}