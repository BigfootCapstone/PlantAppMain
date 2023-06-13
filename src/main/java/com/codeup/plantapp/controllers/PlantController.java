package com.codeup.plantapp.controllers;

import com.codeup.plantapp.models.GardenPlant;
import com.codeup.plantapp.models.Plant;
import com.codeup.plantapp.models.User;
import com.codeup.plantapp.models.sun_amount;
import com.codeup.plantapp.repositories.GardenPlantRepository;
import com.codeup.plantapp.repositories.PlantRepository;
import com.codeup.plantapp.repositories.UserRepository;
import com.codeup.plantapp.services.Keys;
import com.codeup.plantapp.util.PlantDTO;
import com.codeup.plantapp.util.PlantResultDTO;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;

@Controller
public class PlantController {

    private final UserRepository usersDao;
    private final PlantRepository plantsDao;
    private final GardenPlantRepository gardenPlantsDao;

    @Autowired
    private Keys keys;

    private static final String TREFLE_API_URL = "https://trefle.io/api/v1/plants/search";

    private final RestTemplate restTemplate;

    public PlantController(UserRepository usersDao, PlantRepository plantsDao, GardenPlantRepository gardenPlantsDao) {
        this.restTemplate = new RestTemplate();
        this.usersDao = usersDao;
        this.plantsDao = plantsDao;
        this.gardenPlantsDao = gardenPlantsDao;
    }

    @GetMapping("/search")
    public String showSearchForm() {
        return "searchForm";
    }

    @PostMapping("/search")
    public String searchPlants(@RequestParam("query") String query, Model model) {
        String apiUrl = TREFLE_API_URL + "?token=" + keys.getTrefle() + "&q=" + query;
        PlantResultDTO plantResult = restTemplate.getForObject(apiUrl, PlantResultDTO.class);

        model.addAttribute("query", query);
        assert plantResult != null;
        model.addAttribute("plants", plantResult.getData());

        return "searchResults";
    }

    @GetMapping("/plants/{id}")
    public String showPlantDetails(@PathVariable("id") String id, Model model) {
        String apiUrl = "https://trefle.io/api/v1/plants/" + id + "?token=" + keys.getTrefle();

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

// Parse JSON response
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());


            JSONObject plantObject = (JSONObject) jsonResponse.get("data");
            long plant_id = (long) plantObject.get("id");
            String plant_id_string = Long.toString(plant_id);
            String common_name = (String) plantObject.get("common_name");
            String scientific_name = (String) plantObject.get("scientific_name");
            JSONObject family = (JSONObject) plantObject.get("family");
            String family_name = (String) family.get("name");
            JSONObject genus = (JSONObject) plantObject.get("genus");
            String genus_name = (String) genus.get("name");
            String image_url = (String) plantObject.get("image_url");
            String family_common_name = (String) plantObject.get("family_common_name");

            JSONObject mainSpeciesObject = (JSONObject) plantObject.get("main_species");

            String duration = null;
            if (mainSpeciesObject.containsKey("duration")) {
                duration = (String) mainSpeciesObject.get("duration");
            }
            System.out.println("Duration: " + duration);

            String description = null;
            if (mainSpeciesObject.containsKey("growth")) {
                JSONObject growthObject = (JSONObject) mainSpeciesObject.get("growth");
                if (growthObject.containsKey("description")) {
                    description = (String) growthObject.get("description");
                }
            }
            System.out.println("Description: " + description);

            String growth_habit = null;
            if (mainSpeciesObject.containsKey("specifications")) {
                JSONObject specificationsObject = (JSONObject) mainSpeciesObject.get("specifications");
                if (specificationsObject.containsKey("growth_habit")) {
                    growth_habit = (String) specificationsObject.get("growth_habit");
                }
            }
            System.out.println("Growth Habit: " + growth_habit);

            Boolean edible = (Boolean) mainSpeciesObject.get("edible");
            System.out.println("Edible: " + edible);

            PlantDTO plant = new PlantDTO(plant_id_string, common_name, family_name, genus_name, image_url, scientific_name,
                    family_common_name, duration, growth_habit, edible.toString(), description);

            model.addAttribute("plant", plant);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return "view-more";
    }


    @PostMapping("/plants/{id}")
    public String savePlant(@PathVariable("id") String id,
                            @RequestParam(name="name") String plant_name,
                            @RequestParam(name="sun_amount") sun_amount sun_amount,
                            @RequestParam(name="water_interval") long water_interval,
                            @RequestParam(name="is_outside") boolean is_outside
                            ) {
        Plant userPlant = new Plant(id, plant_name);
        plantsDao.save(userPlant);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Date date = new Date();

        GardenPlant newGardenPlant = new GardenPlant(user, userPlant, sun_amount, date, water_interval, is_outside);

        gardenPlantsDao.save(newGardenPlant);
        return "searchForm";
    }

}