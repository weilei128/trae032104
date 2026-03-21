package com.shop.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.shop.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private static final String CSV_FILE = "data/products.csv";
    private List<Product> products = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadProducts();
    }

    private void loadProducts() {
        products.clear();
        try {
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            if (file.exists()) {
                try (CSVReader reader = new CSVReader(new FileReader(file))) {
                    String[] line;
                    reader.readNext();
                    while ((line = reader.readNext()) != null) {
                        Product product = new Product(
                            Long.parseLong(line[0]),
                            line[1],
                            new BigDecimal(line[2]),
                            Integer.parseInt(line[3]),
                            line[4]
                        );
                        products.add(product);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProducts() {
        try {
            File file = ResourceUtils.getFile("classpath:" + CSV_FILE);
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                writer.writeNext(new String[]{"id", "name", "price", "stock", "description"});
                for (Product product : products) {
                    writer.writeNext(new String[]{
                        String.valueOf(product.getId()),
                        product.getName(),
                        product.getPrice().toString(),
                        String.valueOf(product.getStock()),
                        product.getDescription()
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    public Optional<Product> findById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            Long maxId = products.stream().mapToLong(Product::getId).max().orElse(0);
            product.setId(maxId + 1);
            products.add(product);
        } else {
            products.removeIf(p -> p.getId().equals(product.getId()));
            products.add(product);
        }
        saveProducts();
        return product;
    }

    public void deleteById(Long id) {
        products.removeIf(p -> p.getId().equals(id));
        saveProducts();
    }

    public boolean updateStock(Long productId, int quantity) {
        Optional<Product> opt = findById(productId);
        if (opt.isPresent()) {
            Product product = opt.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                saveProducts();
                return true;
            }
        }
        return false;
    }
}
