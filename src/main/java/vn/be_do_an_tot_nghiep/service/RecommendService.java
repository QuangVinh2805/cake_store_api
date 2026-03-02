package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.model.ProductTaste;
import vn.be_do_an_tot_nghiep.repository.ProductRepository;
import vn.be_do_an_tot_nghiep.repository.ProductTasteRepository;
import vn.be_do_an_tot_nghiep.response.RecommendProductResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductTasteRepository productTasteRepository;

    public List<RecommendProductResponse> recommend(String hashId, int limit) {

        Product currentProduct = productRepository.findByHashId(hashId);

        Long categoryId = currentProduct.getCategory().getId();

        // lấy 1 taste đại diện
        String taste = productTasteRepository
                .findProductTasteByProductId(currentProduct.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(ProductTaste::getTaste)
                .orElse(null);

        Set<Product> products = new LinkedHashSet<>();

        // 1. category + taste
        if (taste != null) {
            products.addAll(productRepository.findByCategoryAndTaste(
                    categoryId, taste, hashId,
                    PageRequest.of(0, limit)
            ));
        }

        // 2. category
        if (products.size() < limit) {
            products.addAll(productRepository.findByCategory(
                    categoryId, hashId,
                    PageRequest.of(0, limit - products.size())
            ));
        }

        // 3. random fallback
        if (products.size() < limit) {
            products.addAll(productRepository.findRandom(
                    hashId,
                    PageRequest.of(0, limit - products.size())
            ));
        }

        return products.stream()
                .limit(limit)
                .map(this::mapToResponse)
                .toList();
    }

    private RecommendProductResponse mapToResponse(Product product) {

        ProductTaste taste = productTasteRepository
                .findProductTasteByProductId(product.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);

        return new RecommendProductResponse(
                product.getHashId(),
                taste != null && taste.getImage() != null
                        ? taste.getImage()
                        : product.getImage(),
                taste != null ? taste.getTaste() : null,
                taste != null ? taste.getPrice() : product.getPrice()
        );
    }
}

