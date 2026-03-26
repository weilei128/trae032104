package com.shop.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.shop.entity.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            if (file.exists()) {
                try (CSVReader reader = new CSVReader(new FileReader(file))) {
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

    private void saveOrders() {
        try {
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                writer.writeNext(new String[]{"id", "userId", "productId", "productName", "quantity", "totalPrice", "orderTime"});
                for (Order order : orders) {
                    writer.writeNext(new String[]{
                        String.valueOf(order.getId()),
                        String.valueOf(order.getUserId()),
                        String.valueOf(order.getProductId()),
                        order.getProductName(),
                        String.valueOf(order.getQuantity()),
                        order.getTotalPrice().toString(),
                        order.getOrderTime().format(FORMATTER)
                    });
                }
            }
        } catch (IOException e) {
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
        saveOrders();
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
