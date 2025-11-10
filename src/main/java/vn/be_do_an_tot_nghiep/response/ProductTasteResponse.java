package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTasteResponse {
    private String taste;
    private String image;
    private Long price;
    private String secondDes;
    private Long quantity;
    private String productHashId;
}
