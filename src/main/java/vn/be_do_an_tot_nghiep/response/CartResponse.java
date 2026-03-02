package vn.be_do_an_tot_nghiep.response;

import lombok.Data;

@Data
public class CartResponse {
    private String userName;
    private String productName;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;
    private String image;
    private Long productTasteId;

}
