package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponse {
    private String name;
    private String hashId;
    private String firstDes;
    private Long price;
    private String categoryName;
    private Long categoryId;
    private Long statusFavourite;
    private List<ProductTasteResponse> tastes;
}
