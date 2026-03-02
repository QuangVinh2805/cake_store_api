package vn.be_do_an_tot_nghiep.response;

import lombok.Data;

@Data
public class OrderDetailResponse {
    private String taste;
    private Long quantity;
    private Long totalPrice;
    private String image;
}

