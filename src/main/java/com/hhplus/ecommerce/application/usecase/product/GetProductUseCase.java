package com.hhplus.ecommerce.application.usecase.product;

import com.hhplus.ecommerce.application.query.GetProductQuery;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * 상품 단건 조회 UseCase
 *
 * User Story: "사용자가 상품 상세 정보를 조회한다"
 */
@Service
public class GetProductUseCase {

    private final ProductService productService;

    public GetProductUseCase(ProductService productService) {
        this.productService = productService;
    }

    public Product execute(GetProductQuery query) {
        return productService.getProduct(query.productId());
    }
}
