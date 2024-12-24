package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.Review;
import com.ecom.model.UserDtls;

import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.ReviewService;
import com.ecom.service.SendEmailService2;
import com.ecom.service.UserService;
import com.ecom.service.sendEmailService;
import com.ecom.service.sendPasswordEmailService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OTPUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;
    
    @Value("${spring.mail.username}")
    private String fromEmailId;
    
    @Autowired
    private sendEmailService sendEmailService;
    
    @Autowired
    private sendPasswordEmailService sendPasswordEmailService;
    
    
    
    @Autowired
    private SendEmailService2 sendEmailService2;

    
    private String generatedOtp; 

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @ModelAttribute
    public void getUserDetails(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            m.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            m.addAttribute("countCart", countCart);
        }

        // Fetch all active categories
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categories", allActiveCategory);
    }

    @GetMapping("/")
    public String index(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();

        // Fetch top products (most recent or featured) for the homepage
        List<Product> topProducts = productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8)
                .toList();

        // Add attributes for view
        m.addAttribute("pageTitle", "KrishiMitra");
        m.addAttribute("products", topProducts);
        m.addAttribute("categories", categories);

        m.addAttribute("activePage", "home");
        // Pass the selected category if it's present
        if (category != null && !category.isEmpty()) {
            m.addAttribute("selectedCategory", category);
            m.addAttribute("paramValue", category);
        }

        return "index";
    }
    
    
  
    
    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @GetMapping("/contact")
    public String contact(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();

        // Fetch top products (most recent or featured) for the homepage
        List<Product> topProducts = productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8)
                .toList();

        // Add attributes for view
        m.addAttribute("products", topProducts);
        m.addAttribute("categories", categories);
        m.addAttribute("pageTitle", "Contact");
        m.addAttribute("activePage", "contact");

        // Pass the selected category if it's present
        if (category != null && !category.isEmpty()) {
            m.addAttribute("selectedCategory", category);
            m.addAttribute("paramValue", category);
        }
        return "contact";
    }

    @GetMapping("/signin")
    public String login(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();

        // Fetch top products (most recent or featured) for the homepage
        List<Product> topProducts = productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8)
                .toList();

        // Add attributes for view
        m.addAttribute("pageTitle", "Signin");
        m.addAttribute("products", topProducts);
        m.addAttribute("categories", categories);

        // Pass the selected category if it's present
        if (category != null && !category.isEmpty()) {
            m.addAttribute("selectedCategory", category);
            m.addAttribute("paramValue", category);
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();

        // Fetch top products (most recent or featured) for the homepage
        List<Product> topProducts = productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8)
                .toList();

        // Add attributes for view
        m.addAttribute("pageTitle", "Register");
        m.addAttribute("products", topProducts);
        m.addAttribute("categories", categories);

        // Pass the selected category if it's present
        if (category != null && !category.isEmpty()) {
            m.addAttribute("selectedCategory", category);
            m.addAttribute("paramValue", category);
        }
        return "register";
    }

    @GetMapping("/products")
    public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                           @RequestParam(defaultValue = "") String ch,
                           Model model) {

        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);
        model.addAttribute("activePage", "shop");

        model.addAttribute("searchTerm", ch);

        Page<Product> page = null;
        if (StringUtils.isEmpty(ch)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        } else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();
        m.addAttribute("pageTitle", "Products");
        m.addAttribute("products", products);
        m.addAttribute("productsSize", products.size());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        if (category != null && !category.isEmpty()) {
            model.addAttribute("selectedCategory", category);
        }

        return "product";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model m, @RequestParam(value = "category", defaultValue = "") String category) {
        Product productById = productService.getProductById(id);
        m.addAttribute("product", productById);

        // Fetch all active categories
        List<Category> categories = categoryService.getAllActiveCategory();

        // Fetch top products (most recent or featured) for the homepage
        List<Product> topProducts = productService.getAllActiveProducts("")
                .stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .limit(8)
                .toList();

        m.addAttribute("products", topProducts);
        m.addAttribute("categories", categories);
        m.addAttribute("pageTitle", productById.getTitle());

        // Pass the selected category if it's present
        if (category != null && !category.isEmpty()) {
            m.addAttribute("selectedCategory", category);
            m.addAttribute("paramValue", category);
        }

        return "view_product";
    }

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable int id, @ModelAttribute Review newReview) {
        Product productById = productService.getProductById(id);
        newReview.setProduct(productById);
        reviewService.saveReview(newReview);
        return "redirect:/product/" + id;
    }


    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file,
            RedirectAttributes redirectAttributes) throws IOException {

// Check if email already exists
Boolean existsEmail = userService.existsEmail(user.getEmail());

if (existsEmail) {
redirectAttributes.addFlashAttribute("errorMsg", "Email already exists.");
return "redirect:/register"; // Redirect to registration page
}


// Generate OTP and send to email
generatedOtp = UUID.randomUUID().toString().substring(0, 6); // Simple 6 digit OTP


sendEmailService.sendEmail(user.getEmail(), generatedOtp); 

// Store user details temporarily (without saving to DB)
userService.saveUser(user);  // You can save user data here
redirectAttributes.addFlashAttribute("msg", "OTP Sent Successfully");
// Redirect to OTP verification page
return "redirect:/verifyOtp";
}

// OTP Verification Page
@GetMapping("/verifyOtp")
public String otpVerificationPage(Model model) {
	model.addAttribute("pageTitle", "OTP Verification");
	
return "verifyOtp"; // This is the OTP verification page
}

// Verify OTP entered by user
@PostMapping("/verifyOtp")
public String verifyOtp(@RequestParam("otp") String enteredOtp, @ModelAttribute UserDtls user, RedirectAttributes redirectAttributes) {
    if (generatedOtp != null && generatedOtp.equals(enteredOtp)) {
        // OTP is correct, complete registration and send "Thank you for registering" email
//        sendEmailService.sendEmail(user.getEmail(), "Thank you for registering!", 
//                "Hi ,\n\nThank you for registering an account with us. We are excited to have you onboard!\n\nBest regards,\nYour Company");
        redirectAttributes.addFlashAttribute("msg", "OTP Verified Successfully. You can now log in.");
        return "redirect:/signin"; // Redirect to login page
    } else {
        redirectAttributes.addFlashAttribute("errorMsg", "Invalid OTP.");
        return "redirect:/verifyOtp"; // Redirect to OTP page
    }
}








@GetMapping("/forgot-password")
public String showForgotPasswordPage() {
    return "forgot_password"; // Return the forgot password page
}

@PostMapping("/forgot-password")
public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) {
    // Check if the email exists in the database
    UserDtls user = userService.getUserByEmail(email);
    
    if (user == null) {
        session.setAttribute("errorMsg", "This email is not registered with us.");
        return "redirect:/forgot-password"; // Stay on the same page with error message
    }

    try {
        // Generate a reset token
        String resetToken = UUID.randomUUID().toString();
        userService.updateUserResetToken(email, resetToken);

        // Generate the reset password link
        String resetUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "") + "/reset-password?token=" + resetToken;

        // Send the reset link to the user's email
        sendPasswordEmailService.sendPasswordResetLink(email, resetUrl);

        // Set a success message indicating that the email has been sent
        session.setAttribute("succMsg", "A password reset link has been sent to your email address.");

        return "redirect:/forgot-password";  // Redirect back to the forgot password page with a success message

    } catch (Exception e) {
        session.setAttribute("errorMsg", "An error occurred while processing your request. Please try again.");
        return "redirect:/forgot-password";  // Redirect back to the forgot password page with error message
    }
}

@GetMapping("/reset-password")
public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
    // Check if the reset token is valid
    UserDtls user = userService.getUserByToken(token);
    if (user == null) {
        model.addAttribute("msg", "Your reset link is invalid or expired.");
        return "message"; // Return an error page
    }
    
    model.addAttribute("token", token);
    return "reset_password"; // Show the reset password form
}
@PostMapping("/reset-password")
public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String password,
                                   RedirectAttributes redirectAttributes) {
    // Find user by reset token
    UserDtls user = userService.getUserByToken(token);
    if (user == null) {
        redirectAttributes.addFlashAttribute("errorMsg", "Your reset link is invalid or expired.");
        return "redirect:/signin"; // Redirect to login page with an error message
    }

    // Update the user's password
    user.setPassword(passwordEncoder.encode(password)); // Encode the password
    user.setResetToken(null); // Remove the reset token
    userService.updateUser(user); // Update user details in the database

    redirectAttributes.addFlashAttribute("msg", "Your password has been successfully reset.");
    return "redirect:/signin"; // Redirect to login page
}


    @GetMapping("/search")
    public String searchProduct(@RequestParam String ch, Model m) {
        List<Product> searchProducts = productService.searchProduct(ch);
        m.addAttribute("products", searchProducts);
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);

        return "product";
    }
}
