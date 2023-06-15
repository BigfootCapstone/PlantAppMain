package com.codeup.plantapp.controllers;

import com.codeup.plantapp.models.*;
import com.codeup.plantapp.repositories.GardenPlantRepository;
import com.codeup.plantapp.repositories.PlantRepository;
import com.codeup.plantapp.repositories.UserRepository;
import com.codeup.plantapp.services.Keys;
import com.codeup.plantapp.services.UserServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import static com.codeup.plantapp.services.WeatherCall.getWeather;
import static com.codeup.plantapp.util.CareTips.checkForOutdoorPlants;
import static com.codeup.plantapp.util.CareTips.plantTipCheck;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository usersDao;
    private final PasswordEncoder passwordEncoder;
    private final GardenPlantRepository gardenPlantDao;
    private final PlantRepository plantDao;


//    private final UserRepository userDao;

    public UserController(UserRepository usersDao, GardenPlantRepository gardenPlantDao,
                          PlantRepository plantDao, PasswordEncoder passwordEncoder){
        this.usersDao = usersDao;
        this.gardenPlantDao = gardenPlantDao;
        this.passwordEncoder = passwordEncoder;
        this.plantDao =  plantDao;
    }

    @Autowired
    private Keys keys;

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "createUserForm";
    }

    //messing with it for the create user
    @PostMapping("/create")
    public String createUserProfile(@ModelAttribute("user") User user) {
        user.setCreated_at(LocalDate.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersDao.save(user);
        return "redirect:/users/" + user.getId();
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginSessionSetter(Model model, HttpSession session){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        session.setAttribute("user", user);
        return "redirect:/users/profile";
    }
//    @GetMapping("/logout")
//    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null){
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//        }
//        return "redirect:/";
//    }

    @GetMapping("/profile")
    public String showProfile(Model model) throws Exception{
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long userId = user.getId();
        User userFromDb = usersDao.findUserById(userId);
        model.addAttribute("user", userFromDb);

//      Get Weather for User's City
        String city = userFromDb.getCity();
        String key = keys.getOpenWeather();
        URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key +
                "&units=imperial");
        Weather userWeather = getWeather(url);

//      Aggregate all plants in user's garden
        List<GardenPlant> allPlants = gardenPlantDao.findGardenPlantByUser(userFromDb);

//      Run conditional logic for all plants based on Days passed
        for (GardenPlant plant: allPlants) {
            GardenPlant gardenPlant = gardenPlantDao.findGardenPlantsById(plant.getId());
            GardenPlant checked = plantTipCheck(gardenPlant, userWeather);
            gardenPlantDao.save(checked);
        }

//      Set Weather for View
        model.addAttribute("weather", userWeather);

//      Set Outdoor plants for quick weather reference and warnings
        model.addAttribute("outdoorPlants", checkForOutdoorPlants(userFromDb));

//      Set All Plants for Garden Preview
        model.addAttribute("userPlants", allPlants);

        System.out.println(userFromDb.getUsername());
        return "userProfile";
    }

    @GetMapping("/{id}")
    public String getUserProfile(@PathVariable("id") long id, Model model) {
        User user = usersDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        return "userProfile";
    }

    @GetMapping("/{id}/edit")
    public String editUserProfileForm(@PathVariable(name = "id") Long id, Model model) {
        User user = usersDao.findUserById(id);
        model.addAttribute("user", user);
        return "editUserForm";
    }

    @PostMapping("/{id}/edit")
    public String updateUserProfile(@PathVariable(name = "id") Long id, @ModelAttribute User updatedUser) {
        User user = usersDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        user.setUsername(updatedUser.getUsername());
        user.setFirst_name(updatedUser.getFirst_name());
        user.setLast_name(updatedUser.getLast_name());
        user.setCity(updatedUser.getCity());
        user.setEmail(updatedUser.getEmail());
        usersDao.save(user);
        return "redirect:/users/" + id;
    }


//    @PostMapping("/{id}/delete")
//    public String deleteUserProfile(@PathVariable long id) {
//        usersDao.deleteById(id);
//        return "redirect:/users";
//    }

    // maybe keep - connecting to user services
    @DeleteMapping("/{id}/delete")
    public void deleteEmployee(@PathVariable("id") int id) {
//        UserServices.deleteEmployeeById(id);
    }

    @GetMapping
    public String getAllUsers(Model model) {
        model.addAttribute("users", usersDao.findAll());
        return "users";
    }
}