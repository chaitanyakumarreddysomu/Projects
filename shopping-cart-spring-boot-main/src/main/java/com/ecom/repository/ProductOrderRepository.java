package com.ecom.repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

	List<ProductOrder> findByUserId(Integer userId);

	ProductOrder findByOrderId(String orderId);
	
	Page<ProductOrder> findBySellerId(Integer sellerId, Pageable pageable);
	
	@Query("SELECT COUNT(o) FROM ProductOrder o WHERE o.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Integer sellerId);
	
	Page<ProductOrder> findByProductSellerId(int sellerId, Pageable pageable);
	
	Page<ProductOrder> findByProductIdIn(List<Integer> productIds, Pageable pageable);
	


}
