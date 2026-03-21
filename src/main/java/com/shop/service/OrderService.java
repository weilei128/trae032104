package com.shop.service;

import com.opencsv.CSVReader;
import com.shop.entity.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final String CSV_FILE = "data/orders.csv";
    private List<Order> orders = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        loadOrders();
    }

    private void loadOrders() {
        orders.clear();
        try {
            ClassPathResource resource = new ClassPathResource(CSV_FILE);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     CSVReader reader = new CSVReader(isr)) {
                    String[] line;
                    reader.readNext();
                    while ((line = reader.readNext()) != null) {
                        Order order = new Order(
                            Long.parseLong(line[0]),
                            Long.parseLong(line[1]),
                            Long.parseLong(line[2]),
                            line[3],
                            Integer.parseInt(line[4]),
                            new BigDecimal(line[5]),
                            LocalDateTime.parse(line[6], FORMATTER)
                        );
                        orders.add(order);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            Long maxId = orders.stream().mapToLong(Order::getId).max().orElse(0);
            order.setId(maxId + 1);
        }
        if (order.getOrderTime() == null) {
            order.setOrderTime(LocalDateTime.now());
        }
        orders.add(order);
        return order;
    }

    public List<Order> findByUserId(Long userId) {
        return orders.stream()
            .filter(o -> o.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }
}
