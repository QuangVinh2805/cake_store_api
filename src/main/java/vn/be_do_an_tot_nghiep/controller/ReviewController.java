package vn.be_do_an_tot_nghiep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.request.CreateReviewRequest;
import vn.be_do_an_tot_nghiep.response.AllReviewResponse;
import vn.be_do_an_tot_nghiep.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> review(
            @RequestParam String token,
            @ModelAttribute CreateReviewRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        reviewService.review(token, request,image);
        return ResponseEntity.ok("Đánh giá thành công");
    }

    @GetMapping
    public ResponseEntity<?> getReviews(
            @RequestParam Long productTasteId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviews(productTasteId)
        );
    }

    @GetMapping("/avg")
    public ResponseEntity<?> avgRate(
            @RequestParam Long productTasteId
    ) {
        return ResponseEntity.ok(
                reviewService.avgRate(productTasteId)
        );
    }

    @GetMapping("/product")
    public ResponseEntity<?> getReviewsByProduct(
            @RequestParam String hashId
    ) {
        return ResponseEntity.ok(
                reviewService.getReviewsByProductHashId(hashId)
        );
    }

    @GetMapping("/product/avg")
    public ResponseEntity<?> avgRateByProduct(
            @RequestParam String hashId
    ) {
        return ResponseEntity.ok(
                reviewService.avgRateByProductHashId(hashId)
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<List<AllReviewResponse>> getLatestReviews() {
        return ResponseEntity.ok(reviewService.getLatestReviews());
    }
}

