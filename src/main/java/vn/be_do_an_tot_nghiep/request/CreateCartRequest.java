package vn.be_do_an_tot_nghiep.request;

import lombok.Data;

@Data
public class CreateCartRequest {
    private String token;
    private Long productTasteId;
    private Long quantity;
    private Long unitPrice;
}
