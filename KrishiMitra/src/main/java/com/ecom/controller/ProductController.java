package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.model.Product;
import com.ecom.service.ProductService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/api/products/suggest")
    public List<Product> getProductSuggestions(@RequestParam String query) {
        Pageable pageable = PageRequest.of(0, 4); // Get top 4 products
        Page<Product> productsPage = productService.searchProductsByTitleOrderedByDiscount(query, pageable);
        return productsPage.getContent();
    }
    
}
