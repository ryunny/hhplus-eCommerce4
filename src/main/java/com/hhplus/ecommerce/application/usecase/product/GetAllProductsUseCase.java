package com.hhplus.ecommerce.application.usecase.product;

import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 전체 상품 조회 UseCase
 *
 * User Story: "사용자가 전체 상품 목록을 조회한다"
 */
@Service
public class GetAllProductsUseCase {

    private final ProductService productService;

    public GetAllProductsUseCase(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> execute() {
        return productService.getAllProducts();
    }
}
