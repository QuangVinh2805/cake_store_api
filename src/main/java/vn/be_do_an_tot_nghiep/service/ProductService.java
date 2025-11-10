package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.*;
import vn.be_do_an_tot_nghiep.repository.*;
import vn.be_do_an_tot_nghiep.request.FavouriteProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductRequest;
import vn.be_do_an_tot_nghiep.request.ProductTasteRequest;
import vn.be_do_an_tot_nghiep.response.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    UserRepository userRepository;

    public List<ProductTagResponse> getAll() {
        return productTagRepository.findAllWithTags();
    }

    public ProductResponse getByHashId(String hashId) {
        Product product = productRepository.findByHashId(hashId);
        if (product == null) throw new RuntimeException("Product not found with hashId " + hashId);
        return toResponse(product);
    }

    public ProductResponse create(ProductRequest req, MultipartFile image) throws IOException {
        Product product = new Product();
        product.setName(req.getName());
        product.setPrice(req.getPrice());
        product.setFirstDes(req.getFirstDes());
//        product.setSecondDes(req.getSecondDes());

        // ✅ Sinh hashId duy nhất
        product.setHashId(generateUniqueHashId());

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

    public ProductTagResponse create(Long productId, String tagValue) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + productId));

        ProductTag tag = new ProductTag();
        tag.setProductId(product);
        tag.setTag(tagValue);

        ProductTag saved = productTagRepository.save(tag);
        return toResponse(saved);
    }

    public ProductTagResponse update(Long id, String tagValue) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductTag not found with id " + id));
        tag.setTag(tagValue);
        ProductTag updated = productTagRepository.save(tag);
        return toResponse(updated);
    }

    public void delete(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductTag not found with id " + id));
        productTagRepository.delete(tag);
    }

    public List<ProductTagResponse> getProductTagsByTagId(Long tagId) {
        List<ProductTag> productTags = productTagRepository.findByTagId_Id(tagId);

        return productTags.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

    public List<Tag> getAllTag() {
        return tagRepository.findAll();
    }

    public List<ProductTagResponse> getProductByCategoryId(Long categoryId){
        return productRepository.findProductsByCategoryId(categoryId);
    }

    public List<ProductTagResponse> getProductsByCategoryAndTag(Long categoryId, Long tagId) {
        return productRepository.findProductsByCategoryAndTag(categoryId, tagId);
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
        fav.setRating(rating);
        fav.setContent(content);
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




    public ProductDetailResponse getProductDetailByHashId(String hashId) {
        Product product = productRepository.findByHashId(hashId);

        if (product == null) {
            throw new RuntimeException("Product not found with hashId: " + hashId);
        }

        List<ProductTasteResponse> tasteResponses = new ArrayList<>();
        if (product.getTastes() != null) {
            for (ProductTaste taste : product.getTastes()) {
                ProductTasteResponse tasteRes = new ProductTasteResponse();
                tasteRes.setTaste(taste.getTaste());
                tasteRes.setImage(taste.getImage());
                tasteRes.setPrice(taste.getPrice());
                tasteRes.setSecondDes(taste.getSecondDes());
                tasteResponses.add(tasteRes);
            }
        }

        ProductDetailResponse response = new ProductDetailResponse();
        response.setName(product.getName());
        response.setHashId(product.getHashId());
        response.setFirstDes(product.getFirstDes());
        response.setTastes(tasteResponses);

        return response;
    }

    public ProductTasteResponse createProductTaste(ProductTasteRequest req, MultipartFile image) throws IOException {
        // ✅ Tìm product theo hashId
        Product product = productRepository.findByHashId(req.getProductHashId());
        if (product == null) {
            throw new RuntimeException("Product not found with hashId: " + req.getProductHashId());
        }

        // ✅ Tạo entity taste
        ProductTaste taste = new ProductTaste();
        taste.setTaste(req.getTaste());
        taste.setQuantity(req.getQuantity());
        taste.setPrice(req.getPrice());
        taste.setSecondDes(req.getSecondDes());
        taste.setProduct(product);
        taste.setCreatedAt(new Date());

        // ✅ Upload ảnh nếu có
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
        return toResponse(saved);
    }

    private ProductTasteResponse toResponse(ProductTaste t) {
        ProductTasteResponse res = new ProductTasteResponse();
        res.setTaste(t.getTaste());
        res.setPrice(t.getPrice());
        res.setQuantity(t.getQuantity());
        res.setSecondDes(t.getSecondDes());
        res.setImage(t.getImage());
        res.setProductHashId(t.getProduct().getHashId());
        return res;
    }
}
