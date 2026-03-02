package vn.be_do_an_tot_nghiep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.response.RecommendProductResponse;
import vn.be_do_an_tot_nghiep.service.RecommendService;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    RecommendService recommendService;

    @GetMapping("/{hashId}")
    public List<RecommendProductResponse> recommend(
            @PathVariable String hashId
    ) {
        return recommendService.recommend(hashId, 10);
    }
}

