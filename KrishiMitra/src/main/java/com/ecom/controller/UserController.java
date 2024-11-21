package com.ecom.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.exceptions.ResourceNotFoundException;
import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;



	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
    
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
		
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart saveCart = cartService.saveCart(pid, uid);

		if (ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Product add to cart failed");
		} else {
			session.setAttribute("succMsg", "Product added to cart");
		}
		return "redirect:/product/" + pid;
	}
	

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

	    UserDtls user = getLoggedInUserDetails(p);
	    List<Cart> carts = cartService.getCartsByUser(user.getId());

	    // Add carts to model
	    m.addAttribute("carts", carts);
	    m.addAttribute("pageTitle", "Cart");

	    if (carts.size() > 0) {
	        // Calculate total order price and round to 0 decimal points
	        Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
	        totalOrderPrice = (double) Math.round(totalOrderPrice);
	        m.addAttribute("totalOrderPrice", totalOrderPrice);

	        // Calculate the number of distinct items in the cart
	        int numberOfItems = carts.size(); // Number of distinct items
	        m.addAttribute("numberOfItems", numberOfItems);

	        // Round total price for each cart item
	        for (Cart cart : carts) {
	            cart.setTotalPrice((double) Math.round(cart.getTotalPrice()));
	        }
	    }
	    

	    return "/user/cart";
	}


	@GetMapping("/cartRemove")
	public String removeFromCart(@RequestParam Integer cid) {
	    cartService.removeFromCart(cid); // Assuming you have this method in your service
	    return "redirect:/user/cart";
	}

	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy, cid);
		return "redirect:/user/cart";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
	    UserDtls user = getLoggedInUserDetails(p);
	    List<Cart> carts = cartService.getCartsByUser(user.getId());
	    m.addAttribute("carts", carts);
	    m.addAttribute("pageTitle", "Orders");

	    if (carts.size() > 0) {
	        // Get the total order price
	        Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
	        
	        // Calculate shipping: Free if the order price is > 300, otherwise 20
	        Double shippingCost = orderPrice > 300 ? 0.0 : 20.0;

	        // Calculate tax: 5% of the order price, rounded to 1 decimal place
	        BigDecimal taxAmount = new BigDecimal(orderPrice * 0.05).setScale(1, RoundingMode.HALF_UP);

	        // Calculate total order price including shipping and tax
	        Double totalOrderPrice = orderPrice + shippingCost + taxAmount.doubleValue();

	        // Round total price to the nearest whole number (no decimals)
	        BigDecimal totalOrderPriceRounded = new BigDecimal(totalOrderPrice).setScale(0, RoundingMode.HALF_UP);

	        // Add the calculated values to the model
	        m.addAttribute("orderPrice", orderPrice);
	        m.addAttribute("shippingCost", shippingCost);
	        m.addAttribute("taxAmount", taxAmount);
	        m.addAttribute("totalOrderPrice", totalOrderPriceRounded.intValue());  // Add total order price as an integer
	    }

	    return "/user/order";
	}


	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws Exception {
	    UserDtls user = getLoggedInUserDetails(p);

	    // Ensure that orderService.saveOrder does not contain any email logic
	    orderService.saveOrder(user.getId(), request);

	    return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess(Model m) {
		m.addAttribute("pageTitle", "Success");
	    return "/user/success";
	}
	

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/profile";
	}

	@GetMapping("/profile")
	public String profile(Model m, Principal p) {
	    UserDtls loginUser = getLoggedInUserDetails(p);
	    List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());

	    // Sort orders by order date in descending order
	    orders.sort(Comparator.comparing(ProductOrder::getOrderDate).reversed());

	    m.addAttribute("orders", orders);
	    m.addAttribute("pageTitle", "Profile");
	    return "/user/profile";
	}


//	@PostMapping("/update-profile")
//	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
//		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
//		if (ObjectUtils.isEmpty(updateUserProfile)) {
//			session.setAttribute("errorMsg", "Profile not updated");
//		} else {
//			session.setAttribute("succMsg", "Profile Updated");
//		}
//		return "redirect:/user/profile";
//	}
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
	    // Debugging: Check if the ID is not null
	    if (user.getId() == null) {
	        System.out.println("User ID is null. Cannot update profile.");
	        session.setAttribute("errorMsg", "User ID is missing. Cannot update profile.");
	        return "redirect:/user/profile"; // Or handle appropriately
	    }

	    System.out.println("Updating user with ID: " + user.getId());
	    UserDtls updateUserProfile = userService.updateUserProfile(user, img);
	    if (updateUserProfile == null) {
	        session.setAttribute("errorMsg", "Profile not updated");
	    } else {
	        session.setAttribute("succMsg", "Profile Updated");
	    }
	    return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			UserDtls updateUser = userService.updateUser(loggedInUserDetails);
			if (ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("errorMsg", "Password not updated !! Error in server");
			} else {
				session.setAttribute("succMsg", "Password Updated sucessfully");
			}
		} else {
			session.setAttribute("errorMsg", "Current Password incorrect");
		}

		return "redirect:/user/profile";
	}
	
	

}
