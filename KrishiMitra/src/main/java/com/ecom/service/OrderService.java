package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

import com.razorpay.Order;

public interface OrderService {

	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception;

	public List<ProductOrder> getOrdersByUser(Integer userId);

	public ProductOrder updateOrderStatus(Integer id, String status);

	public List<ProductOrder> getAllOrders();

	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);
	
	public Page<ProductOrder> getOrdersBySellerIdPagination(Integer sellerId, Integer pageNo, Integer pageSize);
	
	long getTotalOrdersBySellerId(Integer sellerId);
	
	public Page<ProductOrder> getOrdersForSeller(int sellerId, int pageNo, int pageSize);
	
	Page<ProductOrder> getOrdersBySellerIdPagination1(int sellerId, int pageNo, int pageSize);
	
	 ProductOrder getOrderById(Integer id);
	
	 long countOrdersByStatus(String status);
	
	 public double getTotalIncomeByStatus(String status);
	 
	 
	
		
}
