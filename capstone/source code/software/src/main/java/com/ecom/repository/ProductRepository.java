package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	List<Product> findByIsActiveTrue();

	Page<Product> findByIsActiveTrue(Pageable pageable);

	List<Product> findByCategory(String category);

	List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);

	Page<Product> findByCategory(Pageable pageable, String category);

	Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

	Page<Product> findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);
	
	Page<Product> findBySellerId(Integer sellerId, Pageable pageable);
	Page<Product> findBySellerIdAndTitleContaining(Integer sellerId, String title, Pageable pageable);
	
	@Query("SELECT p.id FROM Product p WHERE p.sellerId = :sellerId")
    List<Integer> findProductIdsBySellerId(@Param("sellerId") int sellerId);
	
	long countBySellerId(long sellerId);
	Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);
	List<Product> findByTitleContainingIgnoreCase(String title);
	
	Page<Product> findByTitleContainingIgnoreCaseOrderByDiscountDesc(String title, Pageable pageable);
	
}

