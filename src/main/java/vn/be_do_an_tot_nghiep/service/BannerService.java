package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.be_do_an_tot_nghiep.model.Banner;
import vn.be_do_an_tot_nghiep.repository.BannerRepository;
import vn.be_do_an_tot_nghiep.request.BannerRequest;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    /* ================= GET ALL ================= */
    public Page<Banner> getAllPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bannerRepository.findAll(pageable);
    }


    public List<Banner> getAll() {
        return bannerRepository.findAll();
    }



    /* ================= GET ALL BY STATUS ================= */
    public List<Banner> getAllByStatus(Integer status) {
        return bannerRepository.findByStatus(status);
    }

    public Banner create(BannerRequest bannerRequest, MultipartFile image) throws IOException {
        Banner banner = new Banner();
        banner.setProductName(bannerRequest.getProductName());
        banner.setDescription(bannerRequest.getDescription());
        banner.setBackground(bannerRequest.getBackground());
        banner.setColorButton(bannerRequest.getColorButton());
        banner.setStatus(1L);
        if (image != null && !image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/products/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            banner.setImage("/uploads/products/" + fileName);
        }
        banner.setCreatedAt(new Date());
        return bannerRepository.save(banner);
    }

    public Banner update(Long id,BannerRequest bannerRequest, MultipartFile image) throws IOException {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        banner.setProductName(bannerRequest.getProductName());
        banner.setDescription(bannerRequest.getDescription());
        banner.setBackground(bannerRequest.getBackground());
        banner.setColorButton(bannerRequest.getColorButton());
        if (image != null && !image.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/product/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            banner.setImage("/uploads/product/" + fileName);
        }
        banner.setUpdatedAt(new Date());
        return bannerRepository.save(banner);
    }

    /* ================= CHANGE STATUS ================= */
    public void changeStatus(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner không tồn tại"));

        banner.setStatus(banner.getStatus() == 1L ? 0L : 1L);
        bannerRepository.save(banner);
    }
}
