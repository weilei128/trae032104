package com.shop.controller;

import com.shop.annotation.RequireRole;
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
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/shop")
    public ModelAndView shopPage(HttpServletRequest request, HttpSession session) {
        ModelAndView mav = new ModelAndView("shop");
        User user = getUserFromRequest(request, session);
        mav.addObject("user", user);
        List<Product> products = productService.findAll();
        mav.addObject("products", products);
        return mav;
    }

    @GetMapping("/my-orders")
    public ModelAndView myOrdersPage(HttpServletRequest request, HttpSession session) {
        ModelAndView mav = new ModelAndView("my-orders");
        User user = getUserFromRequest(request, session);
        mav.addObject("user", user);
        mav.addObject("orders", orderService.findByUserId(user.getId()));
        return mav;
    }

    @PostMapping("/order")
    @ResponseBody
    @RequireRole
    public Map<String, Object> createOrder(@RequestParam Long productId, @RequestParam Integer quantity, 
                                           HttpServletRequest request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = getUserFromRequest(request, session);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Optional<Product> productOpt = productService.findById(productId);
        if (!productOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "商品不存在");
            return result;
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            result.put("success", false);
            result.put("message", "库存不足");
            return result;
        }

        boolean updated = productService.updateStock(productId, quantity);
        if (!updated) {
            result.put("success", false);
            result.put("message", "库存更新失败");
            return result;
        }

        com.shop.entity.Order order = new com.shop.entity.Order();
        order.setUserId(user.getId());
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice().multiply(new BigDecimal(quantity)));
        orderService.save(order);

        result.put("success", true);
        result.put("message", "下单成功");
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
