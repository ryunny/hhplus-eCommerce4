package com.hhplus.ecommerce.domain.service;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.repository.OrderItemRepository;
import com.hhplus.ecommerce.domain.repository.ProductRepository;
import com.hhplus.ecommerce.domain.vo.Quantity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 상품 조회
     *
     * @param productId 상품 ID
     * @return 상품 엔티티
     */
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
    }

    /**
     * 여러 상품 조회
     *
     * @param productIds 상품 ID 목록
     * @return 상품 엔티티 목록
     */
    public List<Product> getProducts(List<Long> productIds) {
        List<Product> products = new ArrayList<>();
        for (Long productId : productIds) {
            products.add(getProduct(productId));
        }
        return products;
    }

    /**
     * 재고 충분성 검증
     *
     * @param product 상품
     * @param quantity 요청 수량
     * @throws IllegalStateException 재고가 부족한 경우
     */
    public void validateStock(Product product, Quantity quantity) {
        if (!product.hasSufficientStock(quantity)) {
            throw new IllegalStateException("재고가 부족합니다: " + product.getName());
        }
    }

    /**
     * 재고 차감
     * 비즈니스 로직(재고 차감)과 영속화를 함께 처리
     *
     * DB 전환 시: Repository의 findById에 @Lock(LockModeType.PESSIMISTIC_WRITE) 적용 필요
     *
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     */
    public void decreaseStock(Long productId, Quantity quantity) {
        Product product = getProduct(productId);
        product.decreaseStock(quantity);
        productRepository.save(product);
    }

    /**
     * 재고 복구 (주문 취소 시 사용)
     *
     * @param productId 상품 ID
     * @param quantity 복구할 수량
     */
    public void increaseStock(Long productId, Quantity quantity) {
        Product product = getProduct(productId);
        product.increaseStock(quantity);
        productRepository.save(product);
    }

    /**
     * 모든 상품 조회
     *
     * @return 전체 상품 목록
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * 인기 상품 조회 (개선된 메서드)
     * 최근 3일간 판매량 기준 상위 N개 상품의 통계를 반환합니다.
     *
     * Repository에서 DTO를 직접 반환하므로 중복 연산이 제거되었습니다.
     *
     * @param limit 조회할 상품 개수
     * @return 상품 판매 통계 DTO 목록
     */
    public List<ProductSalesDto> getTopSellingProducts(int limit) {
        return orderItemRepository.getTopSellingProducts(limit);
    }
}
