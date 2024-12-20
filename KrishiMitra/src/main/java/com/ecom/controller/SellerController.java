package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.SeleniumService;
import com.ecom.service.SeleniumService.MarketData;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void getSellerDetails(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            m.addAttribute("user", userDtls);
        }

        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categories", allActiveCategory);
    }

    @GetMapping("/")
    public String index(Model model,@AuthenticationPrincipal UserDetails userDetails,Principal principal,
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10000000") int pageSize) {
    	String email = userDetails.getUsername();
    	UserDtls loggedInUser = userService.getUserByEmail(email);
    	Long sellerId = Long.valueOf(loggedInUser.getId());
        long totalProducts = productService.getTotalProductCountBySellerId(sellerId);
        model.addAttribute("totalProducts", totalProducts);
        
        
       

        // Fetch orders for the seller's products
        Page<ProductOrder> page = orderService.getOrdersBySellerIdPagination1(loggedInUser.getId(), pageNo, pageSize);
        model.addAttribute("pageTitle", "Seller Dashboard");
        model.addAttribute("orders", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        
        
        return "seller/index";
    }




    
    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model, 
                                  @AuthenticationPrincipal UserDetails userDetails) {
        
        // Retrieve the logged-in user's email and details
        String email = userDetails.getUsername();
        UserDtls loggedInUser = userService.getUserByEmail(email);
        
        // Fetch categories for the dropdown
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);
        model.addAttribute("sellerName", loggedInUser.getName());
        
        // Set the page title
        model.addAttribute("pageTitle", "Add Product");

        return "seller/add_product"; // Return the view for adding a product
    }

    

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("file") MultipartFile image,@RequestParam("quality") String quality,
                              HttpSession session, Principal p) throws IOException {

        // Handle image name, defaulting to "default.jpg" if no image is uploaded
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        // Get the logged-in user's email to set the seller ID
        String email = p.getName();
        UserDtls loggedInUser = userService.getUserByEmail(email);
        product.setSellerId(loggedInUser.getId()); // Set seller ID

        // Ensure to set the commodity and priceLastUpdated
        product.setCommodity(product.getCommodity()); // Set commodity field
        product.setPriceLastUpdated(product.getPriceLastUpdated()); // Set price last updated

        product.setQuality(quality);
        
        // Save the product to the database
        Product savedProduct = productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(savedProduct)) {
            // Define the file path to save the uploaded image
            File saveFile = new ClassPathResource("static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator + imageName);
            
            // Copy the image file to the specified location
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            
            // Set a success message in the session
            session.setAttribute("succMsg", "Product Saved Successfully");
        } else {
            // Set an error message in case of failure
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/seller/products"; // Redirect to the products list page
    }


    @GetMapping("/products")
    public String loadProducts(Model model,
                               @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "1000000000") int pageSize,
                               @RequestParam(name = "ch", defaultValue = "") String searchTerm,
                               @AuthenticationPrincipal UserDetails userDetails) {

        // Fetch the logged-in user's email
        String email = userDetails.getUsername();
        
        // Get user details using the email
        UserDtls loggedInUser = userService.getUserByEmail(email);

        // Fetch products specific to the logged-in seller
        Page<Product> page = productService.getProductsBySellerIdPagination(loggedInUser.getId(), pageNo, pageSize, searchTerm);

        // Convert ID to Long if necessary
        Long sellerId = Long.valueOf(loggedInUser.getId());

        // Fetch the total count of all products for the logged-in seller
        long totalProducts = productService.getTotalProductCountBySellerId(sellerId);
        
        // Add attributes to the model
        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("pageTitle", "Products");

        return "seller/products";
    }
    
    

    
    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete success");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/seller/products";
	}
    
    @GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		m.addAttribute("pageTitle", "Edit Product");
		return "seller/edit_product";
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
//		return "redirect:/seller/editProduct/" + product.getId();
		return "redirect:/seller/products" ;
	}

    @GetMapping("/orders")
    public String loadOrders(Model model, Principal principal,
                             @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                             @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        String email = principal.getName();
        UserDtls loggedInUser = userService.getUserByEmail(email);

        // Fetch orders for the seller's products
        Page<ProductOrder> page = orderService.getOrdersBySellerIdPagination1(loggedInUser.getId(), pageNo, pageSize);
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("orders", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        
        
        

        return "seller/orders";
    }
    
    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam Integer id, 
                                     @RequestParam Integer st, 
                                     HttpSession session, 
                                     @AuthenticationPrincipal UserDetails userDetails) {
        // Retrieve the logged-in user's email
        String email = userDetails.getUsername();
        UserDtls loggedInUser = userService.getUserByEmail(email);

        // Find the order using the service
        ProductOrder order = orderService.getOrderById(id);

        // Check if the order exists and belongs to the logged-in seller
        if (order == null || !order.getProduct().getSellerId().equals(loggedInUser.getId())) {
            session.setAttribute("errorMsg", "Order not found or unauthorized access.");
            return "redirect:/seller/index"; // Redirect to an appropriate page
        }

        // Retrieve the status from the enum based on the provided ID
        OrderStatus[] values = OrderStatus.values();
        String status = null;
        for (OrderStatus orderSt : values) {
            if (orderSt.getId().equals(st)) {
                status = orderSt.getName();
                break; // Exit the loop once the status is found
            }
        }

        if (status == null) {
            session.setAttribute("errorMsg", "Invalid status.");
            return "redirect:/seller/index"; // Redirect to an appropriate page
        }

        // Update the order status
        ProductOrder updatedOrder = orderService.updateOrderStatus(id, status);

        // Send email notification
        // Implement email notification logic here if needed

        // Set success or error message
        if (updatedOrder != null) {
            session.setAttribute("succMsg", "Status Updated Successfully.");
        } else {
            session.setAttribute("errorMsg", "Failed to update status.");
        }

        // Redirect to the intended page after the update
        return "redirect:/seller/"; // Update this path based on where you want to redirect
    }

    

    @GetMapping("/dashboard")
    public String dashboard(Model m, Principal p) {
        String email = p.getName();
        UserDtls loggedInUser = userService.getUserByEmail(email);

        long totalOrders = orderService.getTotalOrdersBySellerId(loggedInUser.getId());

        m.addAttribute("totalOrders", totalOrders);

        return "seller/dashboard";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) {
        UserDtls updateUserProfile = userService.updateUserProfile(user, img);
        if (ObjectUtils.isEmpty(updateUserProfile)) {
            session.setAttribute("errorMsg", "Profile not updated");
        } else {
            session.setAttribute("succMsg", "Profile Updated");
        }
        return "redirect:/seller/profile";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
    	model.addAttribute("pageTitle", "Profile");
        return "seller/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
                                 HttpSession session) {
        UserDtls loggedInUserDetails = userService.getUserByEmail(p.getName());
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

        if (matches) {
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);
            if (ObjectUtils.isEmpty(updateUser)) {
                session.setAttribute("errorMsg", "Password not updated. Error in server");
            } else {
                session.setAttribute("succMsg", "Password Updated Successfully");
            }
        } else {
            session.setAttribute("errorMsg", "Current Password incorrect");
        }

        return "redirect:/seller/profile";
    }
}