package com.ecom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.exceptions.CouponExpiredException;
import com.ecom.exceptions.CouponNotFoundException;
import com.ecom.model.Coupon;
import com.ecom.repository.CouponRepository;
import com.ecom.util.DateUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public Coupon addCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }
    // Method to save or update a coupon
    public Coupon saveCoupon(Coupon coupon) {
    	  
        return couponRepository.save(coupon);
        
    }

    public Coupon validateCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new RuntimeException("Invalid coupon code");
        }

        // Check if coupon is expired
        if (coupon.getExpiresOn().before(new Date())) {
            throw new RuntimeException("Coupon has expired");
        }

        return coupon;
    }
    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
 
        return coupons;
    }
    
    
    
 // Method to get a coupon by ID
    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id).orElse(null);
    }

    // Method to edit a coupon
    public Coupon editCoupon(Long id, Coupon updatedCoupon) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isPresent()) {
            Coupon existingCoupon = coupon.get();
            existingCoupon.setCode(updatedCoupon.getCode());
            existingCoupon.setDiscountPercentage(updatedCoupon.getDiscountPercentage());
            existingCoupon.setExpiresOn(updatedCoupon.getExpiresOn());
            existingCoupon.setOneTimeUse(updatedCoupon.isOneTimeUse());
            return couponRepository.save(existingCoupon);
        }
        return null;
    }

    // Method to delete a coupon
    public Boolean deleteCoupon(long id) {
        try {
            Optional<Coupon> coupon = couponRepository.findById(id);
            if (coupon.isPresent()) {
                couponRepository.delete(coupon.get());

                return true;  // Successfully deleted
            }
            return false; // Coupon not found, nothing to delete
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // Something went wrong during deletion
        }
    }
    
    
   

 
    

  

}

