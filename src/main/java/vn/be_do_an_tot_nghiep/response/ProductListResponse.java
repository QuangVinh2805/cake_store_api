package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponse {
    private List<String> tags;
    private String productHashId;
    private String productName;
    private String image;
    private Long price;
    private Long categoryId;
    private String categoryName;
    private Long status;
}
