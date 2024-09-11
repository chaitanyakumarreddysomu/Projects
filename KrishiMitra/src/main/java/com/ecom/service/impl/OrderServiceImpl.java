package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Service
public class OrderServiceImpl implements OrderService {

	private static final String KEY="rzp_test_D1DIAIAHMS0aZp";
	private static final String KEY_SECRET="vxyPTcJeduvdsS8xQJPnriqE";
	private static final String CURRENCY="INR";
	
	@Autowired
	private ProductOrderRepository orderRepository;
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

	@Override
	public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {

		List<Cart> carts = cartRepository.findByUserId(userid);

		for (Cart cart : carts) {

			ProductOrder order = new ProductOrder();

			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());

			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());

			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			ProductOrder saveOrder = orderRepository.save(order);
//			commonUtil.sendMailForProductOrder(saveOrder, "success");
		}
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		List<ProductOrder> orders = orderRepository.findByUserId(userId);
		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);

	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

	@Override
	public Page<ProductOrder> getOrdersBySellerIdPagination(Integer sellerId, Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findBySellerId(sellerId, pageable);
	}
	

	@Override
	public long getTotalOrdersBySellerId(Integer sellerId) {
		return orderRepository.countBySellerId(sellerId);
	}
	@Override
	public Page<ProductOrder> getOrdersForSeller(int sellerId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return orderRepository.findByProductSellerId(sellerId, pageable);
    }
	
	
	
	@Override
	public Page<ProductOrder> getOrdersBySellerIdPagination1(int sellerId, int pageNo, int pageSize) {
	    // Fetch product IDs for the given seller
	    List<Integer> productIds = productRepository.findProductIdsBySellerId(sellerId);

	    // Fetch orders containing these product IDs
	    return orderRepository.findByProductIdIn(productIds, PageRequest.of(pageNo, pageSize));
	}
	
	 public ProductOrder getOrderById(Integer id) {
	        return orderRepository.findById(id).orElse(null);
	    }

	@Override
	public long countOrdersByStatus(String status) {
		return orderRepository.countByStatus(status);
	}
	
	 @Override
	    public double getTotalIncomeByStatus(String status) {
	        Double totalIncome = orderRepository.sumPriceByStatus(status);
	        return (totalIncome != null) ? totalIncome : 0.0; // Handle null if no income
	    }

}