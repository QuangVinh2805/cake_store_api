package vn.be_do_an_tot_nghiep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.*;
import vn.be_do_an_tot_nghiep.repository.*;
import vn.be_do_an_tot_nghiep.request.*;
import vn.be_do_an_tot_nghiep.response.*;
import vn.be_do_an_tot_nghiep.util.RandomUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.antlr.v4.runtime.tree.xpath.XPath.findAll;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductTagRepository productTagRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    FavouriteProductRepository favouriteProductRepository;

    @Autowired
    ProductTasteRepository productTasteRepository;

    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    UserRepository userRepository;

    public Page<ProductListResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rows = productRepository.findAllProductWithGroupedTags(pageable);

        return rows.map(r -> {

            ProductListResponse res = new ProductListResponse();
            res.setProductHashId((String) r[0]);
            res.setProductName((String) r[1]);
            res.setImage((String) r[2]);
            res.setPrice(((Number) r[3]).longValue());
            res.setCategoryId(((Number) r[4]).longValue());
            res.setCategoryName((String) r[5]);
            res.setStatus(((Number) r[6]).longValue());

            String tagsStr = (String) r[7];
            if (tagsStr != null) {
                res.setTags(Arrays.asList(tagsStr.split(",")));
            } else {
                res.setTags(List.of());
            }

            return res;
        });
    }

    public Page<ProductListResponse> getAllProductByStatus(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> rows = productRepository.findAllWithGroupedTagsByStatus(pageable);

        return rows.map(r -> {
            ProductListResponse res = new ProductListResponse();

            res.setProductHashId((String) r[0]);
            res.setProductName((String) r[1]);
            res.setImage((String) r[2]);
            res.setPrice(((Number) r[3]).longValue());
            res.setCategoryId(((Number) r[4]).longValue());
            res.setCategoryName((String) r[5]);
            res.setStatus(((Number) r[6]).longValue());

            String tagsStr = (String) r[7];
            if (tagsStr != null && !tagsStr.isBlank()) {
                res.setTags(Arrays.asList(tagsStr.split(",")));
            } else {
                res.setTags(List.of());
            }

            return res;
        });
    }



    public ProductResponse getByHashId(String hashId) {
        Product product = productRepository.findByHashId(hashId);
        if (product == null) throw new RuntimeException("Product not found with hashId " + hashId);
        return toResponse(product);
    }

    public ProductResponse create(ProductRequest req, MultipartFile image) throws IOException {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }

        if (req.getPrice() == null || req.getPrice() < 0) {
            throw new RuntimeException("Giá sản phẩm không hợp lệ");
        }

        if (req.getCategoryId() != null &&
                !categoryRepository.existsById(req.getCategoryId())) {
            throw new RuntimeException("Category không tồn tại");
        }

        if (image != null && !image.isEmpty()) {
            String contentType = image.getContentType();
            if (!List.of("image/png", "image/jpeg", "image/jpg").contains(contentType)) {
                throw new RuntimeException("Ảnh không đúng định dạng");
            }
        }

        Product product = new Product();
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setFirstDes(req.getFirstDes());
//        product.setSecondDes(req.getSecondDes());

        // ✅ Sinh hashId duy nhất
        product.setHashId(generateUniqueHashId());
        product.setStatus(1L);

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);

        // ✅ Lấy category
        if (req.getCategoryId() != null) {
            Category cate = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(cate);
        }

        // ✅ Lưu ảnh nếu có
        if (image != null && !image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/products/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            product.setImage("/uploads/products/" + fileName);
        }

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }



    public ProductResponse updateByHashId(String hashId, ProductRequest request, MultipartFile image) throws IOException {
        Product product = productRepository.findByHashId(hashId);
        if (product == null)
            throw new RuntimeException("Product not found with hashId " + hashId);

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Tên sản phẩm không được để trống");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new RuntimeException("Giá không hợp lệ");
        }

        if (request.getCategoryId() != null &&
                !categoryRepository.existsById(request.getCategoryId())) {
            throw new RuntimeException("Category không tồn tại");
        }


        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setFirstDes(request.getFirstDes());
//        product.setSecondDes(request.getSecondDes());

        // ✅ Cập nhật category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // ✅ Cập nhật ảnh nếu có
        if (image != null && !image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/product/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            product.setImage("/uploads/product/" + fileName);
        }

        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        return toResponse(updated);
    }



    public void deleteByHashId(String hashId) {
        Product product = productRepository.findByHashId(hashId);
        if (product == null) throw new RuntimeException("Product not found with hashId " + hashId);
        productRepository.delete(product);
    }

    // Chuyển Product → ProductResponse
    public ProductResponse toResponse(Product product) {
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        return new ProductResponse(
                product.getName(),
                product.getPrice(),
                product.getImage(),
                product.getFirstDes(),
//                product.getSecondDes(),
                categoryId,
                product.getHashId()
        );
    }

//    public List<ProductTagResponse> getAllTag() {
//        return productTagRepository.findAll().stream()
//                .map(this::toResponse)
//                .collect(Collectors.toList());
//    }

    public ProductTagResponse getById(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductTag not found with id " + id));
        return toResponse(tag);
    }

    public ProductTagResponse createByHashId(String productHashId, String tagValue) {

        if (productHashId == null || productHashId.isBlank()) {
            throw new RuntimeException("productHashId không được để trống");
        }

        Tag tags = tagRepository.findByTagName(tagValue);

        Product product = productRepository.findByHashId(productHashId);
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm với hashId: " + productHashId);
        }

        if (tagValue.length() > 100) {
            throw new RuntimeException("Tag tối đa 100 ký tự");
        }

        boolean exists = productTagRepository
                .existsByProductId_IdAndTag(product.getId(), tagValue);

        if (exists) {
            throw new RuntimeException("Tag đã tồn tại trên sản phẩm");
        }

        if (tagValue == null || tagValue.isBlank()) {
            throw new RuntimeException("Tag không được để trống");
        }

        ProductTag tag = new ProductTag();
        tag.setProductId(product); // ✅ ManyToOne
        tag.setTag(tagValue);
        tag.setTagId(tags);

        ProductTag saved = productTagRepository.save(tag);
        return toResponse(saved);
    }


    @Transactional
    public List<ProductTagResponse> updateByProductHashId(UpdateProductTagRequest req) {

        // 1️⃣ Tìm product
        Product product = productRepository.findByHashId(req.getProductHashId());
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        // 2️⃣ Validate oldTags
        if (req.getOldTags() == null || req.getOldTags().isEmpty()) {
            throw new RuntimeException("Danh sách tag cũ không được để trống");
        }

        // 3️⃣ Validate newTags
        if (req.getNewTags() == null || req.getNewTags().isEmpty()) {
            throw new RuntimeException("Danh sách tag mới không được để trống");
        }

        List<String> oldTags = req.getOldTags().stream()
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();

        List<String> newTags = req.getNewTags().stream()
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();

        if (oldTags.isEmpty() || newTags.isEmpty()) {
            throw new RuntimeException("Danh sách tag không hợp lệ");
        }

        // 4️⃣ Kiểm tra tag master tồn tại
        for (String tagName : newTags) {
            if (!tagRepository.existsByTagName(tagName)) {
                throw new RuntimeException("Tag không tồn tại: " + tagName);
            }
        }

        // 5️⃣ Xóa các tag cũ
        List<ProductTag> oldProductTags =
                productTagRepository.findAllByProductId_IdAndTagIn(
                        product.getId(), oldTags
                );

        if (oldProductTags.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tag cũ cần cập nhật");
        }

        productTagRepository.deleteAll(oldProductTags);

        // 6️⃣ Gán tag mới
        List<ProductTagResponse> responses = new ArrayList<>();

        for (String tagName : newTags) {

            boolean exists = productTagRepository
                    .existsByProductId_IdAndTag(product.getId(), tagName);

            if (exists) continue;

            Tag tagEntity = tagRepository.findByTagName(tagName);
            if (tagEntity == null) {
                throw new RuntimeException("Tag không tồn tại: " + tagName);
            }

            ProductTag productTag = new ProductTag();
            productTag.setProductId(product);
            productTag.setTag(tagName);
            productTag.setTagId(tagEntity);
            productTag.setCreatedAt(new Date());

            ProductTag saved = productTagRepository.save(productTag);
            responses.add(toResponse(saved));
        }

        if (responses.isEmpty()) {
            throw new RuntimeException("Không có tag nào được cập nhật");
        }

        return responses;
    }






    public void delete(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductTag not found with id " + id));
        productTagRepository.delete(tag);
    }

    public PageResponse<ProductListResponse> getProductByTag(
            Long tagId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> pageData =
                productRepository.findProductsByTagId(tagId, pageable);

        return new PageResponse<>(
                mapToProductListResponse(pageData.getContent()),
                page,
                size,
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    public PageResponse<ProductListResponse> getProductByTagOne(
            Long tagId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> pageData =
                productRepository.findProductsByTagId(tagId, pageable);

        return new PageResponse<>(
                mapToProductListResponse(pageData.getContent()),
                page,
                size,
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    private ProductTagResponse toResponse(ProductTag productTag) {
        Product p = productTag.getProductId();

        Long categoryId = null;
        if (p.getCategory() != null) {
            categoryId = p.getCategory().getId();
        }

        ProductTagResponse res = new ProductTagResponse();
        res.setTag(productTag.getTag());
        res.setProductHashId(p.getHashId());
        res.setProductName(p.getName());
        res.setImage(p.getImage());
        res.setPrice(p.getPrice());
        res.setCategoryId(categoryId);

        return res;
    }

    private String generateUniqueHashId() {
        String hash;
        do {
            hash = randomString(6);
        } while (productRepository.findByHashId(hash) != null);
        return hash;
    }

    private String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Page<Tag> getAllTagPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tagRepository.findAll(pageable);
    }

    public List<Tag> getAllTag() {
        return tagRepository.findAllByStatus();
    }



    public Tag create(Tag tag) {

        // 1. Validate name
        if (tag.getTagName() == null || tag.getTagName().trim().isEmpty()) {
            throw new RuntimeException("Tên tag không được để trống");
        }

        // 2. Check trùng tên
        if (tagRepository.existsByTagNameIgnoreCase(tag.getTagName().trim())) {
            throw new RuntimeException("Tag đã tồn tại");
        }

        // 3. Chuẩn hóa data
        tag.setTagName(tag.getTagName().trim());
        tag.setStatus(1L); // mặc định hiện

        return tagRepository.save(tag);
    }


    public Tag update(Long id, Tag tagE) {

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tag "));

        // 1. Validate name
        if (tagE.getTagName() == null || tagE.getTagName().trim().isEmpty()) {
            throw new RuntimeException("Tên danh mục không được để trống");
        }

        // 2. Check trùng tên (trừ chính nó)
        if (tagRepository.existsByTagNameIgnoreCaseAndIdNot(
                tagE.getTagName().trim(),
                id
        )) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        // 3. Update
        tag.setTagName(tagE.getTagName().trim());

        return tagRepository.save(tag);
    }


    public void changeStatusTag(Long id){
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tag"));

        tag.setStatus(tag.getStatus() == 1L ? 0L : 1L);
        tagRepository.save(tag);
    }

    public PageResponse<ProductListResponse> getProductByCategory(
            Long categoryId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> pageData =
                productRepository.findProductsByCategoryId(categoryId, pageable);

        return new PageResponse<>(
                mapToProductListResponse(pageData.getContent()),
                page,
                size,
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }


    public PageResponse<ProductListResponse> getProductByCategoryAndTag(
            Long categoryId, Long tagId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> pageData =
                productRepository.findProductsByCategoryAndTag(categoryId, tagId, pageable);

        return new PageResponse<>(
                mapToProductListResponse(pageData.getContent()),
                page,
                size,
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }



    public FavouriteProductResponse toggleFavourite(FavouriteProductRequest request) {

        // 🔑 Tìm user theo token
        User user = null;
        for (User u : userRepository.findAll()) {
            if (request.getToken().equals(u.getToken())) {
                user = u;
                break;
            }
        }
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        // 🎬 Tìm product theo hashId
        Product product = productRepository.findByHashId(request.getHashId());
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm với hashId: " + request.getHashId());
        }

        // ❤️ Toggle trạng thái
        FavouriteProduct fav = favouriteProductRepository.findByUser_IdAndProduct_Id(user.getId(), product.getId());

        if (fav != null) {
            fav.setStatus(fav.getStatus() == 1 ? 0L : 1L);
            fav.setUpdatedAt(new Date());
        } else {
            fav = new FavouriteProduct();
            fav.setUser(user);
            fav.setProduct(product);
            fav.setStatus(1L);
            fav.setCreatedAt(new Date());
            fav.setUpdatedAt(new Date());
        }

        favouriteProductRepository.save(fav);

        return new FavouriteProductResponse(request.getToken(), request.getHashId(),product.getName(),product.getPrice(),product.getImage() ,fav.getStatus());
    }

    // ⭐ Gửi đánh giá & bình luận
    public FavouriteProduct rateAndComment(Long userId, Long productId, Long rating, String content) {
        FavouriteProduct fav = favouriteProductRepository.findByUser_IdAndProduct_Id(userId, productId);

        if (fav == null) {
            fav = new FavouriteProduct();
            fav.setUser(userRepository.findById(userId).orElseThrow());
            fav.setProduct(productRepository.findById(productId).orElseThrow());
            fav.setCreatedAt(new Date());
        }

        fav.setStatus(1L);
        fav.setUpdatedAt(new Date());
        return favouriteProductRepository.save(fav);
    }

    public List<FavouriteProductResponse> getByToken(String token) {
        User user = null;
        for (User u : userRepository.findAll()) {
            if (token.equals(u.getToken())) {
                user = u;
                break;
            }
        }

        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        List<FavouriteProduct> favourites = favouriteProductRepository.findByUser_Id(user.getId());

        return favourites.stream()
                .map(f -> new FavouriteProductResponse(
                        token,
                        f.getProduct().getHashId(),
                        f.getProduct().getName(),
                        f.getProduct().getPrice(),
                        f.getProduct().getImage(),
                        f.getStatus()
                ))
                .toList();
    }




    public ProductDetailResponse getProductDetailByHashId(String hashId, String token) {
        Product product = productRepository.findByHashId(hashId);
        if (product == null) {
            throw new RuntimeException("Product not found with hashId: " + hashId);
        }
        // 1. Mặc định là 0 (chưa thích)
        Long statusFavourite = 0L;

        // 2. Nếu có token, tìm user và check bảng FavouriteProduct
        if (token != null && !token.isEmpty()) {
            User user = userRepository.findByToken(token); // Giả định bạn có hàm này, nếu không dùng vòng for như cũ
            if (user != null) {
                FavouriteProduct fav = favouriteProductRepository.findByUser_IdAndProduct_Id(user.getId(), product.getId());
                if (fav != null) {
                    statusFavourite = fav.getStatus();
                }
            }
        }

        // Lấy danh sách tastes (giữ nguyên logic của bạn)
        List<ProductTaste> tastes = productTasteRepository.findByProductId(product.getId());
        List<ProductTasteResponse> tasteResponses = tastes.stream().map(taste -> {
            ProductTasteResponse tasteRes = new ProductTasteResponse();
            tasteRes.setTaste(taste.getTaste());
            tasteRes.setImage(taste.getImage());
            tasteRes.setPrice(taste.getPrice());
            tasteRes.setQuantity(taste.getQuantity());
            tasteRes.setSecondDes(taste.getSecondDes());
            tasteRes.setProductTasteId(taste.getId());
            return tasteRes;
        }).collect(Collectors.toList());

        // 3. Set vào response
        ProductDetailResponse response = new ProductDetailResponse();
        response.setName(product.getName());
        response.setHashId(product.getHashId());
        response.setFirstDes(product.getFirstDes());
        response.setPrice(product.getPrice());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setTastes(tasteResponses);
        response.setStatusFavourite(statusFavourite); // Trả về 1 hoặc 0

        return response;
    }


    public ProductTasteResponse createProductTaste(
            ProductTasteRequest req,
            MultipartFile image
    ) throws IOException {

        Product product = productRepository.findByHashId(req.getProductHashId());
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        if (req.getTaste() == null || req.getTaste().isBlank()) {
            throw new RuntimeException("Taste không được để trống");
        }

        if (req.getPrice() == null || req.getPrice() < 0) {
            throw new RuntimeException("Giá taste không hợp lệ");
        }

        if (req.getQuantity() != null && req.getQuantity() < 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }

        boolean exists = productTasteRepository
                .existsByProductIdAndTaste(product.getId(), req.getTaste());

        if (exists) {
            throw new RuntimeException("Taste đã tồn tại");
        }


        ProductTaste taste = new ProductTaste();
        taste.setProductId(product.getId());
        taste.setTaste(req.getTaste());
        taste.setQuantity(req.getQuantity());
        taste.setPrice(req.getPrice());
        taste.setSecondDes(req.getSecondDes());
        taste.setCreatedAt(new Date());
        taste.setUpdatedAt(new Date());

        if (image != null && !image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/taste/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            taste.setImage("/uploads/taste/" + fileName);
        }

        ProductTaste saved = productTasteRepository.save(taste);

        return toResponse(saved, product);
    }

    public ProductTasteResponse updateProductTasteByTaste(
            ProductTasteRequest req,
            MultipartFile image
    ) throws IOException {

        // 1. Tìm product
        Product product = productRepository.findByHashId(req.getProductHashId());
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        if (req.getPrice() != null && req.getPrice() < 0) {
            throw new RuntimeException("Giá không hợp lệ");
        }

        if (req.getQuantity() != null && req.getQuantity() < 0) {
            throw new RuntimeException("Số lượng không hợp lệ");
        }


        // 2. Tìm taste theo product + taste
        ProductTaste taste = productTasteRepository
                .findByProductIdAndTaste(product.getId(), req.getTaste());

        if (taste == null) {
            throw new RuntimeException("Không tìm thấy vị bánh: " + req.getTaste());
        }
        taste.setTaste(req.getTaste());
        // 3. Update field (chỉ update field nào có)
        if (req.getPrice() != null)
            taste.setPrice(req.getPrice());

        if (req.getQuantity() != null)
            taste.setQuantity(req.getQuantity());

        if (req.getSecondDes() != null)
            taste.setSecondDes(req.getSecondDes());

        taste.setUpdatedAt(new Date());

        // 4. Update image nếu có
        if (image != null && !image.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads/taste/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            taste.setImage("/uploads/taste/" + fileName);
        }

        ProductTaste saved = productTasteRepository.save(taste);
        return toResponse(saved, product);
    }


    public Page<ProductListResponse> searchProductByStatus(
            String keyword,
            int page,
            int size
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> rows =
                productRepository.searchProductByStatus(keyword.trim(), pageable);

        return rows.map(row -> {
            ProductListResponse res = new ProductListResponse();

            res.setProductHashId((String) row[0]); // p.hash_id
            res.setProductName((String) row[1]);   // p.name
            res.setImage((String) row[2]);         // p.image

            // price
            res.setPrice(
                    row[3] != null ? ((Number) row[3]).longValue() : 0L
            );

            // category_id
            res.setCategoryId(
                    row[4] != null ? ((Number) row[4]).longValue() : null
            );

            // category_name (nếu cần)
            res.setCategoryName((String) row[5]);

            // tags
            if (row[7] != null) {
                res.setTags(List.of(row[7].toString().split(",")));
            } else {
                res.setTags(List.of());
            }

            return res;
        });
    }


    public Page<ProductListResponse> searchAllProduct(
            String keyword,
            int page,
            int size
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> rows =
                productRepository.searchAllProduct(keyword.trim(), pageable);

        return rows.map(row -> {
            ProductListResponse res = new ProductListResponse();

            res.setProductHashId((String) row[0]); // p.hash_id
            res.setProductName((String) row[1]);   // p.name
            res.setImage((String) row[2]);         // p.image

            // price
            res.setPrice(
                    row[3] != null ? ((Number) row[3]).longValue() : 0L
            );

            // category_id
            res.setCategoryId(
                    row[4] != null ? ((Number) row[4]).longValue() : null
            );

            // category_name (nếu cần)
            res.setCategoryName((String) row[5]);

            // tags
            if (row[7] != null) {
                res.setTags(List.of(row[7].toString().split(",")));
            } else {
                res.setTags(List.of());
            }

            return res;
        });
    }



    private ProductTasteResponse toResponse(ProductTaste t, Product product) {
        ProductTasteResponse res = new ProductTasteResponse();
        res.setTaste(t.getTaste());
        res.setPrice(t.getPrice());
        res.setQuantity(t.getQuantity());
        res.setSecondDes(t.getSecondDes());
        res.setImage(t.getImage());
        res.setProductHashId(product.getHashId());
        return res;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public String buildProductText() {
        StringBuilder sb = new StringBuilder();

        List<Object[]> data = productTasteRepository.findAllProductWithTaste();

        for (Object[] row : data) {
            Product p = (Product) row[0];
            ProductTaste pt = (ProductTaste) row[1];

            sb.append("- ")
                    .append(p.getName())
                    .append(" | Vị: ")
                    .append(pt.getTaste())
                    .append(" | Giá: ")
                    .append(pt.getPrice())
                    .append("đ");

            if (pt.getSecondDes() != null) {
                sb.append(" | ")
                        .append(pt.getSecondDes());
            } else if (p.getFirstDes() != null) {
                sb.append(" | ")
                        .append(p.getFirstDes());
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public void changeStatus(String hashId){
        Product product = productRepository.findByHashId(hashId);
        if (product == null) {
            throw new RuntimeException("Product not found with hashId: " + hashId);
        }

        product.setStatus(product.getStatus() == 1L ? 0L : 1L);
        productRepository.save(product);
    }

    private List<ProductListResponse> mapToProductListResponse(List<Object[]> rows) {

        Map<String, ProductListResponse> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String hashId = (String) row[0];

            map.putIfAbsent(hashId, new ProductListResponse(
                    new ArrayList<>(),
                    hashId,
                    (String) row[1],
                    (String) row[2],
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue(),
                    (String) row[5],
                    ((Number) row[6]).longValue()
            ));

            String tagName = (String) row[7];
            if (tagName != null) {
                map.get(hashId).getTags().add(tagName);
            }
        }

        return new ArrayList<>(map.values());
    }



}
