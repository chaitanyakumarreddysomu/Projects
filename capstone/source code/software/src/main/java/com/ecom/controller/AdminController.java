package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Coupon;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.CouponService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

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

	@GetMapping("/")
	public String index(Model m, 
	                    @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                    @RequestParam(name = "pageSize", defaultValue = "1000000") Integer pageSize,
	                    @RequestParam(defaultValue = "") String ch) {

	    // For total product count in index page
	    long totalProductCount = productService.getTotalProductCount();
	    m.addAttribute("totalProductCount", totalProductCount);

	    // For total orders count in index page
	    Page<ProductOrder> page1 = orderService.getAllOrdersPagination(pageNo, pageSize);
	    m.addAttribute("totalElements", page1.getTotalElements());

	    // For total category count in index page
	    Page<Category> page2 = categoryService.getAllCategorPagination(pageNo, pageSize);
	    m.addAttribute("totalElements", page2.getTotalElements());

	    // For users count in index page
	    long countType1 = userService.countUsers("ROLE_USER");
	    long countType2 = userService.countUsers("ROLE_SELLER");
	    long countType3 = userService.countUsers("ROLE_ADMIN");

	    m.addAttribute("countType1", countType1);
	    m.addAttribute("countType2", countType2);
	    m.addAttribute("countType3", countType3);
	    m.addAttribute("pageTitle", "Admin Dashboard");

	    // Fetch orders by order service
	    long totalPending = orderService.countOrdersByStatus("In Progress");
	    long totalDelivered = orderService.countOrdersByStatus("DELIVERED");
	    long totalCancelled = orderService.countOrdersByStatus("CANCELLED");

	    m.addAttribute("totalPending", totalPending);
	    m.addAttribute("totalDelivered", totalDelivered);
	    m.addAttribute("totalCancelled", totalCancelled);

	    // Total Income from Delivered Orders
	    m.addAttribute("totalIncome", orderService.getTotalIncomeByStatus("DELIVERED"));

	    // Display orders in admin page, sorted by latest first
	    List<ProductOrder> orders = orderService.getAllOrders(); // Fetch all orders without pagination
	    orders.sort(Comparator.comparing(ProductOrder::getOrderDate).reversed()); // Sort by order date descending
	    m.addAttribute("orders", orders);

	    // Add pagination attributes
	    m.addAttribute("pageNo", page1.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page1.getTotalElements());
	    m.addAttribute("totalPages", page1.getTotalPages());
	    m.addAttribute("isFirst", page1.isFirst());
	    m.addAttribute("isLast", page1.isLast());

	    // Search in admin page
	    Page<Product> page;
	    if (ch != null && !ch.isEmpty()) {
	        page = productService.searchProductPagination(pageNo, pageSize, ch);
	    } else {
	        page = productService.getAllProductsPagination(pageNo, pageSize);
	    }

	    return "admin/index";
	}


	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String categoryPage(Model model, 
	                            @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                            @RequestParam(name = "pageSize", defaultValue = "1000000") Integer pageSize) {
	    Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
	    List<Category> categories = page.getContent();

	    model.addAttribute("categories", categories);
	    model.addAttribute("pageNo", page.getNumber());
	    model.addAttribute("pageSize", pageSize);
	    model.addAttribute("totalElements", page.getTotalElements());
	    model.addAttribute("totalPages", page.getTotalPages());
	    model.addAttribute("isFirst", page.isFirst());
	    model.addAttribute("isLast", page.isLast());
	    model.addAttribute("pageTitle", "Category");

	    return "admin/category";
	}
	@GetMapping("/category/add")
	public String addCategoryForm(Model model) {
	    model.addAttribute("category", new Category());
	    model.addAttribute("pageTitle", "AddCategory");
	    return "admin/add_category";
	}

	@PostMapping("/category/save")
	public String saveCategory(@ModelAttribute Category category, HttpSession session) {
	    Boolean existCategory = categoryService.existCategory(category.getName());

	    if (existCategory) {
	        session.setAttribute("errorMsg", "Category Name already exists");
	    } else {
	        Category savedCategory = categoryService.saveCategory(category);

	        if (savedCategory == null) {
	            session.setAttribute("errorMsg", "Not saved! Internal server error");
	        } else {
	            session.setAttribute("succMsg", "Saved successfully");
	        }
	    }

	    return "redirect:/admin/category";
	}


	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		m.addAttribute("pageTitle", "EditCategory");
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, HttpSession session) {
	    Category oldCategory = categoryService.getCategoryById(category.getId());

	    if (oldCategory != null) {
	        oldCategory.setName(category.getName());
	        oldCategory.setIsActive(category.getIsActive());

	        Category updateCategory = categoryService.saveCategory(oldCategory);

	        if (updateCategory != null) {
	            session.setAttribute("succMsg", "Category update success");
	        } else {
	            session.setAttribute("errorMsg", "Something went wrong on the server");
	        }
	    } else {
	        session.setAttribute("errorMsg", "Category not found");
	    }

	    return "redirect:/admin/loadEditCategory/" + category.getId();
	}


	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ image.getOriginalFilename());

			// System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("succMsg", "Product Saved Success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

	    Page<Product> page;
	    if (ch != null && !ch.isEmpty()) {
	        page = productService.searchProductPagination(pageNo, pageSize, ch);
	    } else {
	        page = productService.getAllProductsPagination(pageNo, pageSize);
	    }
	    
	    long totalProductCount = productService.getTotalProductCount();
	  
	    m.addAttribute("pageTitle", "Products");
	    m.addAttribute("products", page.getContent());
	    m.addAttribute("totalProductCount", totalProductCount);
	    
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", pageSize);
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    return "admin/products";
	}


	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		m.addAttribute("pageTitle", "EditProduct");
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "invalid Discount");
		} else {
			Product updateProduct = productService.updateProduct(product, image);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("succMsg", "Product update success");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model model, @RequestParam Integer type) {
	    List<UserDtls> users = null;
	    long countType1 = userService.countUsers("ROLE_USER");
	    long countType2 = userService.countUsers("ROLE_SELLER");
	    long countType3 = userService.countUsers("ROLE_ADMIN");
	    
	    if (type == 1) {
	        users = userService.getUsers("ROLE_USER");
	        model.addAttribute("pageTitle", "Users");
	    } else if (type == 2) {
	        users = userService.getUsers("ROLE_SELLER");
	        model.addAttribute("pageTitle", "Sellers");
	    } else {
	        users = userService.getUsers("ROLE_ADMIN");
	        model.addAttribute("pageTitle", "Admins");
	    }
	    
	    model.addAttribute("userType", type);
	    model.addAttribute("users", users);
	    model.addAttribute("countType1", countType1);
	    model.addAttribute("countType2", countType2);
	    model.addAttribute("countType3", countType3);
	    
	    return "/admin/users";
	}
	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id,@RequestParam Integer type, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account Status Updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/users?type="+type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model model, 
	                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
	    
	    Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);

	    // Add orders and pagination details to the model
	    model.addAttribute("orders", page.getContent());
	    model.addAttribute("pageNo", page.getNumber());
	    model.addAttribute("pageSize", pageSize);
	    model.addAttribute("totalElements", page.getTotalElements());
	    model.addAttribute("totalPages", page.getTotalPages());
	    model.addAttribute("isFirst", page.isFirst());
	    model.addAttribute("isLast", page.isLast());

	    model.addAttribute("pageTitle", "Orders");
	    // Add counts for each order status
	    model.addAttribute("totalPending", orderService.countOrdersByStatus("PENDING"));
	    model.addAttribute("totalDelivered", orderService.countOrdersByStatus("DELIVERED"));
	    model.addAttribute("totalCancelled", orderService.countOrdersByStatus("CANCELLED"));

	    // Add total income
	    model.addAttribute("totalIncome", orderService.getTotalIncomeByStatus("DELIVERED"));
	    
	    return "admin/orders";
	}

	    @PostMapping("/update-order-status")
	    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

	        OrderStatus[] values = OrderStatus.values();
	        String status = null;

	        for (OrderStatus orderSt : values) {
	            if (orderSt.getId().equals(st)) {
	                status = orderSt.getName();
	            }
	        }

	        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

	        if (updateOrder != null) {
	            session.setAttribute("succMsg", "Status Updated");
	        } else {
	            session.setAttribute("errorMsg", "Status not updated");
	        }
	        return "redirect:/admin/orders";
	    }
	    
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);

			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());

		}
		return "/admin/orders";

	}

	@GetMapping("/add-admin")
	public String loadAdminAdd(Model model) {
		model.addAttribute("pageTitle", "Add Admin");
		return "/admin/add_admin";
	}

	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
	        throws IOException {

	    // Default profile image if no file is uploaded
	    String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    user.setProfileImage(imageName);

	    // Save user information
	    UserDtls saveUser;
	    try {
	        saveUser = userService.saveAdmin(user);
	    } catch (DataIntegrityViolationException e) {
	        // Handle unique constraint violation and other errors
	        session.setAttribute("errorMsg", "Email address already in use.");
	        return "redirect:/admin/add-admin";
	    } catch (Exception e) {
	        // Handle any other exceptions
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	        return "redirect:/admin/add-admin";
	    }

	    // Handle file upload
	    if (saveUser != null) {
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img/profile_img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath(), file.getOriginalFilename());

	            // Ensure directory exists
	            Files.createDirectories(saveFile.toPath());

	            // Save the file to the specified path
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }
	        session.setAttribute("succMsg", "Registered successfully.");
	    } else {
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	    }

	    return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/add-seller")
	public String loadSellerAdd(Model m) {
		m.addAttribute("pageTitle", "Add Seller");
		return "/admin/add_seller";
	}

	@PostMapping("/save-seller")
	public String saveSeller(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
	        throws IOException {

	    // Default profile image if no file is uploaded
	    String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    user.setProfileImage(imageName);

	    // Save user information
	    UserDtls saveUser;
	    try {
	        saveUser = userService.saveSeller(user);
	    } catch (DataIntegrityViolationException e) {
	        // Handle unique constraint violation and other errors
	        session.setAttribute("errorMsg", "Email address already in use.");
	        return "redirect:/admin/add-seller";
	    } catch (Exception e) {
	        // Handle any other exceptions
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	        return "redirect:/admin/add-seller";
	    }

	    // Handle file upload
	    if (saveUser != null) {
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img/profile_img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath(), file.getOriginalFilename());

	            // Ensure directory exists
	            Files.createDirectories(saveFile.toPath());

	            // Save the file to the specified path
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }
	        session.setAttribute("succMsg", "Registered successfully.");
	    } else {
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	    }

	    return "redirect:/admin/add-seller";
	}

	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("pageTitle", "Profile");
		return "/admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			HttpSession session) {
		UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

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

		
		return "redirect:/admin/profile";
	}

	
	@Autowired
    private CouponService couponService;

	@GetMapping("/coupon/add")
	public String addCoupon(Model model) {
		model.addAttribute("pageTitle", "coupon");
		return "/admin/add_coupon";
	}
	
    // Endpoint to add a new coupon
	 @PostMapping("/coupon/add")
	    public String addCoupon(@RequestParam String code,
	                            @RequestParam double discountPercentage,
	                            @RequestParam String expiresOn,
	                            @RequestParam boolean oneTimeUse,
	                            HttpSession session) {

	        // Convert expiresOn string to Date
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	        java.util.Date expirationDate = null;
	        try {
	            expirationDate = sdf.parse(expiresOn);  // Parsing date from string
	        } catch (ParseException e) {
	            session.setAttribute("errorMsg", "Invalid expiration date format");
	            return "redirect:/admin/coupons";  // Redirect back to /admin/coupons on error
	        }

	        // Create Coupon object
	        Coupon coupon = new Coupon();
	        coupon.setCode(code);
	        coupon.setDiscountPercentage(discountPercentage);
	        coupon.setExpiresOn(expirationDate);
	        coupon.setOneTimeUse(oneTimeUse);
	        coupon.setUsed(false);  // Default value for 'used'

	        // Save the coupon using the service
	        Coupon savedCoupon = couponService.addCoupon(coupon);

	        // Check if the coupon was saved successfully or not
	        if (savedCoupon == null) {
	            session.setAttribute("errorMsg", "Not saved! Internal server error");
	        } else {
	            session.setAttribute("succMsg", "Coupon Added successfully");
	        }

	        // Redirect back to /admin/coupons
	        return "redirect:/admin/coupon";
	    }   
	 @GetMapping("/coupon")
	    public String viewCoupons(Model model, HttpSession session) {
	        List<Coupon> coupons = couponService.getAllCoupons();

	        // Log the number of coupons being passed to the model
	

	        // Add coupons and messages to the model
	        model.addAttribute("coupons", coupons);
	        model.addAttribute("succMsg", session.getAttribute("succMsg"));
	        model.addAttribute("errorMsg", session.getAttribute("errorMsg"));

	        // Clear session attributes after displaying them
	        session.removeAttribute("succMsg");
	        session.removeAttribute("errorMsg");

	        return "admin/coupon";  // Thymeleaf template to display coupons
	    }


	 
	 
	 
	 @GetMapping("/coupon/edit/{id}")
	 public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
	     Coupon coupon = couponService.getCouponById(id);
	     if (coupon == null) {
	         session.setAttribute("errorMsg", "Coupon not found");
	         return "redirect:/admin/coupons";  // Redirect back if coupon not found
	     }
	     model.addAttribute("coupon", coupon);
	     return "admin/editCoupon";  // Thymeleaf template for editing the coupon
	 }
	 
	 @GetMapping("/loadEditCoupon/{id}")
		public String loadEditCoupon(@PathVariable Long id, Model m,@ModelAttribute Coupon coupon) {
			m.addAttribute("coupon", couponService.getCouponById(coupon.getId()));
			m.addAttribute("pageTitle", "EditCoupon");
			return "admin/editCoupon";
		}
	 
	 
	 
	   @PostMapping("/updateCoupon")
	    public String updateCoupon(@ModelAttribute Coupon coupon, HttpSession session) {
	        Coupon existingCoupon = couponService.getCouponById(coupon.getId());

	        // If the coupon is not found, show an error message
	        if (existingCoupon != null) {
	            // Update the existing coupon details
	            existingCoupon.setCode(coupon.getCode());
	            existingCoupon.setDiscountPercentage(coupon.getDiscountPercentage());
	            existingCoupon.setExpiresOn(coupon.getExpiresOn());
	            existingCoupon.setOneTimeUse(coupon.isOneTimeUse());

	            // Save the updated coupon
	            Coupon updatedCoupon = couponService.saveCoupon(existingCoupon);

	            // Check if the update was successful
	            if (updatedCoupon != null) {
	                session.setAttribute("succMsg", "Coupon updated successfully");
	            } else {
	                session.setAttribute("errorMsg", "Something went wrong on the server");
	            }
	        } else {
	            session.setAttribute("errorMsg", "Coupon not found");
	        }

	        // Redirect back to the edit form page for the coupon
	        return "redirect:/loadEditCoupon/" + coupon.getId();
	    }

	    // Endpoint to handle editing a coupon
	    @PostMapping("/coupon/edit/{id}")
	    public String editCoupon(@PathVariable Long id,
	                              @RequestParam String code,
	                              @RequestParam double discountPercentage,
	                              @RequestParam String expiresOn,
	                              @RequestParam boolean oneTimeUse,
	                              HttpSession session) {

	        // Convert expiresOn string to Date
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	        java.util.Date expirationDate = null;
	        try {
	            expirationDate = sdf.parse(expiresOn);  // Parsing date from string
	        } catch (ParseException e) {
	            session.setAttribute("errorMsg", "Invalid expiration date format");
	            return "redirect:/coupon/edit/" + id;  // Redirect back to the edit form
	        }

	        // Create an updated coupon object
	        Coupon updatedCoupon = new Coupon();
	        updatedCoupon.setCode(code);
	        updatedCoupon.setDiscountPercentage(discountPercentage);
	        updatedCoupon.setExpiresOn(expirationDate);
	        updatedCoupon.setOneTimeUse(oneTimeUse);

	        // Call service to edit the coupon
	        Coupon savedCoupon = couponService.editCoupon(id, updatedCoupon);

	        // Check if the coupon was updated successfully or not
	        if (savedCoupon == null) {
	            session.setAttribute("errorMsg", "Coupon update failed");
	        } else {
	            session.setAttribute("succMsg", "Coupon updated successfully");
	        }

	        // Redirect to the coupon list
	        return "redirect:/admin/coupon";
	    }

	
	    
		@GetMapping("/deleteCoupon/{id}")
		public String deleteCoupon(@PathVariable long id, HttpSession session) {
			Boolean deleteCoupon = couponService.deleteCoupon(id);

			if (deleteCoupon) {
				session.setAttribute("succMsg", "category delete success");
			} else {
				session.setAttribute("errorMsg", "something wrong on server");
			}

			return "redirect:/admin/coupon";
		}
		
	
	
}
