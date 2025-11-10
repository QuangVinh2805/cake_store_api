package vn.be_do_an_tot_nghiep.request;


import lombok.Data;

@Data
public class ProductTasteRequest {
    private String productHashId;
    private String taste;
    private Long price;
    private Long quantity;
    private String secondDes;
}
