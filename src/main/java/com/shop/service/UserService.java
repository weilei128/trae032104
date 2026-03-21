package com.shop.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.shop.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            if (file.exists()) {
                try (CSVReader reader = new CSVReader(new FileReader(file))) {
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

    private void saveUsers() {
        try {
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                writer.writeNext(new String[]{"id", "username", "password", "admin"});
                for (User user : users) {
                    writer.writeNext(new String[]{
                        String.valueOf(user.getId()),
                        user.getUsername(),
                        user.getPassword(),
                        String.valueOf(user.isAdmin())
                    });
                }
            }
        } catch (IOException e) {
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
