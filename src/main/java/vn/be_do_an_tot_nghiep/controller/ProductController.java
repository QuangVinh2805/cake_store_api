package vn.be_do_an_tot_nghiep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.Category;
import vn.be_do_an_tot_nghiep.model.FavouriteProduct;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.model.Tag;
import vn.be_do_an_tot_nghiep.request.FavouriteProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductTasteRequest;
import vn.be_do_an_tot_nghiep.request.UpdateProductTagRequest;
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
    public Page<ProductListResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return productService.getAll(page, size);
    }

    @GetMapping("/getAllProductByStatus")
    public Page<ProductListResponse> getAllProductsByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return productService.getAllProductByStatus(page, size);
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

    @GetMapping("/getAllTagsPaging")
    public Page<Tag> getAllTagsPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return productService.getAllTagPaging(page, size);
    }

    @GetMapping("/getAllTags")
    public List<Tag> getAllTags() {
        return productService.getAllTag();
    }



    @PostMapping("/tag/create")
    public Tag createTag(@RequestBody Tag tag) {
        return productService.create(tag);
    }

    @PutMapping("/tag/update")
    public ResponseEntity<Tag> updateTag(@RequestParam Long id, @RequestBody Tag tag) {
        try {
            return ResponseEntity.ok(productService.update(id, tag));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
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
    public ProductTagResponse createTag(
            @RequestParam String productHashId,
            @RequestParam String tag
    ) {
        return productService.createByHashId(productHashId, tag);
    }



    @PutMapping("/updateTag")
    public ResponseEntity<List<ProductTagResponse>> updateTag(
            @RequestBody UpdateProductTagRequest request
    ) {
        return ResponseEntity.ok(productService.updateByProductHashId(request));
    }


    @PutMapping("/changeStatusTag")
    public ResponseEntity<String> changeStatusTag(@RequestParam  Long id){
        productService.changeStatusTag(id);
        return ResponseEntity.ok("Đổi trạng thái thành công!");
    }

    @GetMapping("/getProductByTag")
    public ResponseEntity<PageResponse<ProductListResponse>> getProductByTag(@RequestParam Long tagId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.getProductByTag(tagId,page,size));
    }

    @GetMapping("/getProductByTagOne")
    public ResponseEntity<PageResponse<ProductListResponse>> getProductByTagOne(@RequestParam Long tagId,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(productService.getProductByTagOne(tagId,page,size));
    }

    @GetMapping("/getProductByCategoryId")
    public ResponseEntity<PageResponse<ProductListResponse>> getProductByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                productService.getProductByCategory(categoryId, page, size)
        );
    }


    @GetMapping("/getProductByCategoryAndTag")
    public ResponseEntity<PageResponse<ProductListResponse>> getProductByCategoryAndTag(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(productService.getProductByCategoryAndTag(categoryId, tagId, page, size));
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
    public ResponseEntity<?> getProductDetail(
            @RequestParam String hashId,
            @RequestParam(required = false) String token // Thêm token để check favourite
    ) {
        try {
            ProductDetailResponse response = productService.getProductDetailByHashId(hashId, token);
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

    @PutMapping(value = "/taste/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductTasteResponse updateTaste(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ProductTasteRequest req = mapper.readValue(data, ProductTasteRequest.class);

        return productService.updateProductTasteByTaste(req, image);
    }



    @GetMapping("/search")
    public ResponseEntity<Page<?>> searchProduct(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size

    ) {
        return ResponseEntity.ok(
                productService.searchProductByStatus(keyword, page, size)
        );
    }


    @GetMapping("/searchAllProduct")
    public ResponseEntity<Page<?>> searchAllProduct(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size

    ) {
        return ResponseEntity.ok(
                productService.searchAllProduct(keyword, page, size)
        );
    }

    @PutMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestParam  String hashId){
        productService.changeStatus(hashId);
        return ResponseEntity.ok("Đổi trạng thái thành công!");
    }


}
