package com.shop.service;

import com.opencsv.CSVReader;
import com.shop.entity.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
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
            ClassPathResource resource = new ClassPathResource(CSV_FILE);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream();
                     InputStreamReader isr = new InputStreamReader(is);
                     CSVReader reader = new CSVReader(isr)) {
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
        return product;
    }

    public void deleteById(Long id) {
        products.removeIf(p -> p.getId().equals(id));
    }

    public boolean updateStock(Long productId, int quantity) {
        Optional<Product> opt = findById(productId);
        if (opt.isPresent()) {
            Product product = opt.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                return true;
            }
        }
        return false;
    }
}
