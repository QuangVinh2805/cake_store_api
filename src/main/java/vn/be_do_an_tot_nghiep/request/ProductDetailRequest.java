package vn.be_do_an_tot_nghiep.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailRequest {
    private String name;
    private Long price;
    private String firstDes;
    private Long categoryId;
    private List<ProductTasteRequest> tastes;
}
