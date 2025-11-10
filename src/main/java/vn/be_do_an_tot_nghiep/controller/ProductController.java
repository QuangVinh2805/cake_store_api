package vn.be_do_an_tot_nghiep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.FavouriteProduct;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.model.Tag;
import vn.be_do_an_tot_nghiep.request.FavouriteProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductTasteRequest;
import vn.be_do_an_tot_nghiep.response.*;
import vn.be_do_an_tot_nghiep.service.ProductService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductService productService;

    // GET all
    @GetMapping
    public List<ProductTagResponse> getAllProducts() {
        return productService.getAll();
    }

    // GET by hashId
    @GetMapping("/getByHashId")
    public ResponseEntity<ProductResponse> getProductByHashId(@RequestParam String hashId) {
        try {
            ProductResponse product = productService.getByHashId(hashId);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponse createProduct(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProductRequest req;
        try {
            req = mapper.readValue(data, ProductRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
        }

        return productService.create(req, image);
    }



    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestParam String hashId,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProductRequest req;
        try {
            req = mapper.readValue(data, ProductRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
        }

        try {
            ProductResponse updated = productService.updateByHashId(hashId, req, image);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    // DELETE by hashId
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteProduct(@RequestParam String hashId) {
        try {
            productService.deleteByHashId(hashId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getAllTags")
    public List<Tag> getAllTags() {
        return productService.getAllTag();
    }

    // GET by id
    @GetMapping("/getTagById")
    public ResponseEntity<ProductTagResponse> getTagById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(productService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create
    @PostMapping("/createTag")
    public ProductTagResponse createTag(@RequestParam Long productId, @RequestParam String tag) {
        return productService.create(productId, tag);
    }

    // PUT update
    @PutMapping("/updateTag")
    public ResponseEntity<ProductTagResponse> updateTag(@RequestParam Long id, @RequestParam String tag) {
        try {
            return ResponseEntity.ok(productService.update(id, tag));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE
    @DeleteMapping("/deleteTag")
    public ResponseEntity<Void> deleteTag(@RequestParam Long id) {
        try {
            productService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getProductByTag")
    public ResponseEntity<List<ProductTagResponse>> getProductByTag(@RequestParam Long tagId) {
        return ResponseEntity.ok(productService.getProductTagsByTagId(tagId));
    }

    @GetMapping("/getProductByCategoryId")
    public List<ProductTagResponse> getProductByCategoryId(@RequestParam Long categoryId) {
        return productService.getProductByCategoryId(categoryId);
    }

    @GetMapping("/getProductByCategoryAndTag")
    public ResponseEntity<List<ProductTagResponse>> getProductByCategoryAndTag(
            @RequestParam Long categoryId,
            @RequestParam Long tagId
    ) {
        List<ProductTagResponse> result = productService.getProductsByCategoryAndTag(categoryId, tagId);
        return ResponseEntity.ok(result);
    }


    // ❤️ Thích / bỏ thích
    @PostMapping("/favourite")
    public FavouriteProductResponse toggleFavourite(@RequestBody FavouriteProductRequest request) {
        return productService.toggleFavourite(request);
    }

    // ⭐ Đánh giá phim
    @PostMapping("/rate")
    public FavouriteProduct rate(@RequestParam Long userId,
                                 @RequestParam Long productId,
                                 @RequestParam Long rating,
                                 @RequestParam(required = false) String content) {
        return productService.rateAndComment(userId, productId, rating, content);
    }

    @GetMapping("/userFavourite")
    public List<FavouriteProductResponse> getByUser(@RequestParam String token) {
        return productService.getByToken(token);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getProductDetail(@RequestParam String hashId) {
        try {
            ProductDetailResponse response = productService.getProductDetailByHashId(hashId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/taste/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductTasteResponse createTaste(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProductTasteRequest req = mapper.readValue(data, ProductTasteRequest.class);
        return productService.createProductTaste(req, image);
    }

}
