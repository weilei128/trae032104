package com.shop.controller;

import com.shop.annotation.RequireRole;
import com.shop.entity.Order;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequireRole(requireAdmin = true)
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public String adminRedirect() {
        return "redirect:/admin/products";
    }

    @GetMapping("/products")
    public ModelAndView productsPage(HttpServletRequest request, HttpSession session) {
        ModelAndView mav = new ModelAndView("admin-products");
        User user = getUserFromRequest(request, session);
        mav.addObject("user", user);
        List<Product> products = productService.findAll();
        mav.addObject("products", products);
        return mav;
    }

    @GetMapping("/orders")
    public ModelAndView ordersPage(HttpServletRequest request, HttpSession session) {
        ModelAndView mav = new ModelAndView("admin-orders");
        User user = getUserFromRequest(request, session);
        mav.addObject("user", user);
        List<Order> orders = orderService.findAll();
        mav.addObject("orders", orders);
        return mav;
    }

    @PostMapping("/product/add")
    @ResponseBody
    public Map<String, Object> addProduct(@RequestParam String name, @RequestParam BigDecimal price, 
                                          @RequestParam Integer stock, @RequestParam String description) {
        Map<String, Object> result = new HashMap<>();
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(description);
        productService.save(product);
        result.put("success", true);
        return result;
    }

    @PostMapping("/product/update")
    @ResponseBody
    public Map<String, Object> updateProduct(@RequestParam Long id, @RequestParam String name, 
                                             @RequestParam BigDecimal price, @RequestParam Integer stock, 
                                             @RequestParam String description) {
        Map<String, Object> result = new HashMap<>();
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(name);
            product.setPrice(price);
            product.setStock(stock);
            product.setDescription(description);
            productService.save(product);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "商品不存在");
        }
        return result;
    }

    @PostMapping("/product/delete")
    @ResponseBody
    public Map<String, Object> deleteProduct(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        productService.deleteById(id);
        result.put("success", true);
        return result;
    }
    
    private User getUserFromRequest(HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
            if (userId != null) {
                user = new User();
                user.setId(userId);
                user.setUsername(username);
                user.setAdmin(isAdmin != null && isAdmin);
            }
        }
        return user;
    }
}
