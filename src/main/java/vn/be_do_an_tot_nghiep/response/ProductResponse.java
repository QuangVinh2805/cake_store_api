package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String name;
    private Long price;
    private String image;
    private String firstDes;
//    private String secondDes;
    private Long categoryId;
    private String hashId;
}
