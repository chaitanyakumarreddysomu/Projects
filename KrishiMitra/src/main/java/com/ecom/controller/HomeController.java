package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

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
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CartService cartService;

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
            // Add error message if email already exists
            redirectAttributes.addFlashAttribute("errorMsg", "Email already exists");
            return "redirect:/register";
        } else {
            // Handle file upload
            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(imageName);

            try {
                // Save user to the database
                UserDtls saveUser = userService.saveUser(user);

                if (saveUser != null) {
                    if (!file.isEmpty()) {
                        // Define the path where the file will be saved
                        File saveFile = new ClassPathResource("static/img/profile_img").getFile();
                        Path path = Paths.get(saveFile.getAbsolutePath(), imageName);

                        // Ensure directory exists
                        Files.createDirectories(saveFile.toPath().getParent());

                        // Save the file to the specified path
                        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    }
                    // Add success message
                    redirectAttributes.addFlashAttribute("msg", "Registration successful");
                    return "redirect:/signin";
                } else {
                    // Add error message if user could not be saved
                    redirectAttributes.addFlashAttribute("errorMsg", "Something went wrong on the server");
                }
            } catch (IOException e) {
                // Handle file-related errors
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMsg", "Failed to save file: " + e.getMessage());
            } catch (Exception e) {
                // Handle unexpected errors
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
            }
        }

        // Redirect back to the registration page
        return "redirect:/register";
    }

    // Forgot Password Code 
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model m) {
    	m.addAttribute("pageTitle", "Forgot Password");
        return "forgot_password.html";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, HttpServletRequest request) {
        UserDtls userByEmail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid email address.");
            logger.warn("Forgot password request with invalid email: {}", email);
            return "redirect:/forgot-password";
        }

        try {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);

            // Generate reset link
            String resetUrl = commonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
            logger.info("Password reset token generated for email: {}. Reset link: {}", email, resetUrl);

            // Redirect the user to the password reset page with the token
            return "redirect:/reset-password?token=" + resetToken;

        } catch (Exception e) {
            logger.error("Error processing password reset for email: {}", email, e);
            session.setAttribute("errorMsg", "An error occurred while processing your request. Please try again later.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {
        UserDtls userByToken = userService.getUserByToken(token);

        if (userByToken == null) {
            m.addAttribute("msg", "Your link is invalid or expired !!");
            return "message";
        }
        m.addAttribute("token", token);
        m.addAttribute("pageTitle", "Reset Password");
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String password, 
                                HttpSession session, RedirectAttributes redirectAttributes) {
        UserDtls userByToken = userService.getUserByToken(token);
        if (userByToken == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "Your link is invalid or expired !!");
            return "redirect:/signin";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);

            redirectAttributes.addFlashAttribute("msg", "Password changed successfully");
            return "redirect:/signin";
        }
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
