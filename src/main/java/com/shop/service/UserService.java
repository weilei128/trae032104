package com.shop.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.shop.entity.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final String CSV_FILE = "data/users.csv";
    private List<User> users = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadUsers();
    }

    private void loadUsers() {
        users.clear();
        try {
            ClassPathResource resource = new ClassPathResource(CSV_FILE);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     CSVReader reader = new CSVReader(isr)) {
                    String[] line;
                    reader.readNext();
                    while ((line = reader.readNext()) != null) {
                        User user = new User(
                            Long.parseLong(line[0]),
                            line[1],
                            line[2],
                            Boolean.parseBoolean(line[3])
                        );
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<User> login(String username, String password) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst();
    }

    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
