package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTagResponse {
//    private Long id ;
    private String tag;
    private String productHashId;
    private String productName;
    private String image;
    private Long price;
    private Long categoryId;
    private String categoryName;
}
