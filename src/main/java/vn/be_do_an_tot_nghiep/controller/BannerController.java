package vn.be_do_an_tot_nghiep.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.Banner;
import vn.be_do_an_tot_nghiep.request.BannerRequest;
import vn.be_do_an_tot_nghiep.request.ProductRequest;
import vn.be_do_an_tot_nghiep.response.ProductResponse;
import vn.be_do_an_tot_nghiep.service.BannerService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/getAllPaging")
    public Page<Banner> getAllPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return bannerService.getAllPaging(page, size);
    }

    @GetMapping("/getAll") public List<Banner> getAll() {
        return bannerService.getAll();
    }


    @GetMapping("/getAllByStatus")
    public List<Banner> getAllByStatus(@RequestParam Integer status) {
        return bannerService.getAllByStatus(status);
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Banner create(@RequestPart("data") String data,
                         @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            BannerRequest req;
            try {
                req = mapper.readValue(data, BannerRequest.class);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
            }

            return bannerService.create(req, image);
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Banner update(
            @RequestParam Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image
    )
       throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            BannerRequest req;
            try {
                req = mapper.readValue(data, BannerRequest.class);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi đọc dữ liệu JSON", e);
            }

            return bannerService.update(id,req,image);
    }

    @PutMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestParam Long id) {
        bannerService.changeStatus(id);
        return ResponseEntity.ok("Đổi trạng thái banner thành công");
    }
}
