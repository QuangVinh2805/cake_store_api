package vn.be_do_an_tot_nghiep.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.ProductTaste;
import vn.be_do_an_tot_nghiep.model.Review;
import vn.be_do_an_tot_nghiep.model.User;
import vn.be_do_an_tot_nghiep.repository.*;
import vn.be_do_an_tot_nghiep.request.CreateReviewRequest;
import vn.be_do_an_tot_nghiep.response.AllReviewResponse;
import vn.be_do_an_tot_nghiep.response.ReviewResponse;
import vn.be_do_an_tot_nghiep.util.JwtUtil;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private ProductTasteRepository productTasteRepository;
    @Autowired
    private ProductRepository productRepository;

    public void review(String token, CreateReviewRequest request, MultipartFile image) {

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token hết hạn,vui lòng đăng nhập lại!");
        }

        if (request.getRate() < 1 || request.getRate() > 5) {
            throw new RuntimeException("Rate phải từ 1 đến 5");
        }

        String phone = jwtUtil.getPhoneFromToken(token);
        User user = userRepository.findByPhone(phone);

        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        long purchased = orderDetailRepository.countPurchasedProduct(
                user.getId(),
                request.getProductTasteId()
        );

        if (purchased == 0) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sau khi đã mua sản phẩm");
        }

        Review review = reviewRepository
                .findByUserIdAndProductTasteId(user.getId(), request.getProductTasteId());

        if (review == null) {
            review = new Review();
            review.setUserId(user.getId());
            review.setProductTasteId(request.getProductTasteId());
            review.setCreatedAt(new Date());
        }

        review.setComment(request.getComment());
        review.setRate(request.getRate());
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/image/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                File filePath = new File(dir, fileName);
                image.transferTo(filePath);

                // Nếu  đã có anh cũ → có thể xoá nếu muốn
                if (review.getImage() != null) {
                    File oldFile = new File(System.getProperty("user.dir") + review.getImage());
                    if (oldFile.exists()) oldFile.delete();
                }

                review.setImage("/uploads/image/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh", e);
            }
        }
        review.setUpdatedAt(new Date());

        reviewRepository.save(review);
    }

    public List<ReviewResponse> getReviews(Long productTasteId) {

        List<Review> reviews =
                reviewRepository.findByProductTasteIdOrderByCreatedAtDesc(productTasteId);

        return reviews.stream()
                .map(r -> {
                    User u = userRepository.findById(r.getUserId()).orElse(null);
                    ProductTaste productTaste = productTasteRepository.findByProductTasteId(productTasteId);
                    return new ReviewResponse(r, u, productTaste);
                })
                .toList();
    }

    public Double avgRate(Long productTasteId) {
        return reviewRepository.avgRate(productTasteId);
    }


    public List<ReviewResponse> getReviewsByProductHashId(String hashId) {

        List<Review> reviews =
                reviewRepository.findByProductHashId(hashId);

        return reviews.stream()
                .map(r -> {
                    User u = userRepository.findById(r.getUserId()).orElse(null);
                    ProductTaste productTaste = productTasteRepository.findByProductTasteId(r.getProductTasteId());
                    return new ReviewResponse(r, u, productTaste);
                })
                .toList();
    }

    public Double avgRateByProductHashId(String hashId) {
        return reviewRepository.avgRateByProductHashId(hashId);
    }

    public List<AllReviewResponse> getLatestReviews() {
        List<Object[]> rows = reviewRepository.findLatestReviews();

        return rows.stream().map(row -> {
            AllReviewResponse res = new AllReviewResponse();
            res.setReviewId(((Number) row[0]).longValue());
            res.setUserName((String) row[1]);
            res.setUserAvatar((String) row[2]);
            res.setProductName((String) row[3]);
            res.setTasteName((String) row[4]);
            res.setComment((String) row[5]);
            res.setRate(((Number) row[6]).intValue());
            res.setCreatedAt((Date) row[7]);
            return res;
        }).toList();
    }
}
