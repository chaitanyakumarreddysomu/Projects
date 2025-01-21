package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.exceptions.ResourceNotFoundException;
import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {
	    // Fetch user and product
	    UserDtls userDtls = userRepository.findById(userId)
	        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
	    Product product = productRepository.findById(productId)
	        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

	    // Check current cart status
	    Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
	    Cart cart;

	    if (cartStatus == null) {
	        // If not in cart, create new entry
	        cart = new Cart();
	        cart.setProduct(product);
	        cart.setUser(userDtls);
	        cart.setQuantity(1); // Start with a quantity of 1
	    } else {
	        // If already in cart, increment quantity
	        int updatedQuantity = cartStatus.getQuantity() + 1;

	        // Check if the updated quantity exceeds available stock
	        if (updatedQuantity > product.getStock()) {
	            updatedQuantity = product.getStock(); // Limit to available stock
	        }

	        // Update the existing cart item
	        cart = cartStatus;
	        cart.setQuantity(updatedQuantity);
	    }

	    // Calculate total price based on updated quantity
	    cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());
	    
	    // Save the cart item
	    return cartRepository.save(cart);
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();
		for (Cart c : carts) {
			Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice = totalOrderPrice + totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updateCarts.add(c);
		}

		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}
	
	@Override
	public void removeFromCart(Integer cartId) {
	    // Logic to remove the cart item by ID
	    cartRepository.deleteById(cartId);
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
	    Cart cart = cartRepository.findById(cid)
	        .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

	    Product product = cart.getProduct(); // Get the product associated with the cart item
	    int updateQuantity;

	    if (sy.equalsIgnoreCase("de")) {
	        updateQuantity = cart.getQuantity() - 1;

	        if (updateQuantity <= 0) {
	            cartRepository.delete(cart);
	        } else {
	            cart.setQuantity(updateQuantity);
	            cartRepository.save(cart);
	        }
	    } else {
	        // Increment quantity
	        updateQuantity = cart.getQuantity() + 1;

	        // Check if the updated quantity exceeds available stock
	        if (updateQuantity > product.getStock()) {
	            updateQuantity = product.getStock(); // Limit to available stock
	        }

	        cart.setQuantity(updateQuantity);
	        cartRepository.save(cart);
	    }
	}

	
}
