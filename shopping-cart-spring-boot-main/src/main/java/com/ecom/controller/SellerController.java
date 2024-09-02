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
    public String index() {
        return "seller/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername(); // or userDetails.getEmail() if applicable
        UserDtls loggedInUser = userService.getUserByEmail(email);

        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories", categories);
        m.addAttribute("sellerName", loggedInUser.getName()); // Add seller's name

        return "seller/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
                              HttpSession session, Principal p) throws IOException {

        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        String email = p.getName();
        UserDtls loggedInUser = userService.getUserByEmail(email);
        product.setSellerId(loggedInUser.getId()); // Set seller ID

        Product saveProduct = productService.saveProduct(product);

        if (!ObjectUtils.isEmpty(saveProduct)) {
            File saveFile = new ClassPathResource("static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
                    + image.getOriginalFilename());
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            session.setAttribute("succMsg", "Product Saved Successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/seller/products";
    }


    @GetMapping("/products")
    public String loadProducts(Model model,
                               @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
                               @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(name = "ch", defaultValue = "") String searchTerm,
                               @AuthenticationPrincipal UserDetails userDetails) {

        // Fetch the logged-in user's email
        String email = userDetails.getUsername();
        
        // Get user details using the email
        UserDtls loggedInUser = userService.getUserByEmail(email);

        // Fetch products specific to the logged-in seller
        Page<Product> page = productService.getProductsBySellerIdPagination(loggedInUser.getId(), pageNo, pageSize, searchTerm);
        
        // Add attributes to the model
        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElements", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "seller/products";
    }
    
    

    
    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable("id") int id, HttpSession session) {
        boolean deleteProduct = productService.deleteProduct(id);
        if (deleteProduct) {
            session.setAttribute("succMsg", "Product deleted successfully.");
        } else {
            session.setAttribute("errorMsg", "Error occurred while deleting the product.");
        }
        return "redirect:/seller/products"; // Redirect to seller's products page
    }
    
    @GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
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
    public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session, @AuthenticationPrincipal UserDetails userDetails) {
        // Retrieve the logged-in user's email
        String email = userDetails.getUsername();
        UserDtls loggedInUser = userService.getUserByEmail(email);

        // Find the order using the service
        ProductOrder order = orderService.getOrderById(id);

        // Check if the order exists and belongs to the logged-in seller
        if (order == null || !order.getProduct().getSellerId().equals(loggedInUser.getId())) {
            session.setAttribute("errorMsg", "Order not found or unauthorized access.");
            return "redirect:/seller/orders";
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
            return "redirect:/seller/orders";
        }

        // Update the order status
        ProductOrder updatedOrder = orderService.updateOrderStatus(id, status);

        // Send email notification
        

        // Set success or error message
        if (!ObjectUtils.isEmpty(updatedOrder)) {
            session.setAttribute("succMsg", "Status Updated Successfully.");
        } else {
            session.setAttribute("errorMsg", "Failed to update status.");
        }

        return "redirect:/seller/orders";
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
    public String profile() {
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
