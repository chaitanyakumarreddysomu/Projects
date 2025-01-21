package com.ecom.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Product;
import com.ecom.model.Review;
import com.ecom.repository.ReviewRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsForProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }
}