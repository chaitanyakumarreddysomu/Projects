package com.ecom.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ecom.model.Product;

public interface ProductService {


	public Product saveProduct(Product product);

	public List<Product> getAllProducts();

	public Boolean deleteProduct(Integer id);
	

	public Product getProductById(Integer id);

	public Product updateProduct(Product product, MultipartFile file);

	public List<Product> getAllActiveProducts(String category);

	public List<Product> searchProduct(String ch);


	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category);

	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch);

	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize);
	
	public Page<Product> getProductsBySellerIdPagination(Integer sellerId, Integer pageNo, Integer pageSize);

	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch);
	
	public Page<Product> getProductsBySellerIdPagination(Integer sellerId, int pageNo, int pageSize, String searchTerm);
	long getTotalProductCount();//admin
	
	long getTotalProductCountBySellerId(long sellerId);

	Page<Product> searchProductsByTitle(String title, Pageable pageable);
	
	Page<Product> searchProductsByTitleOrderedByDiscount(String title, Pageable pageable);
	

	public Page<Product> searchProducts(String query, Pageable pageable);
}
