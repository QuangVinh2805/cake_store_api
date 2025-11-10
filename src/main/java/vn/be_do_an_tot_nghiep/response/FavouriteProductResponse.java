package vn.be_do_an_tot_nghiep.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavouriteProductResponse {
    private String token;
    private String hashId;
    private String name;
    private Long price;
    private String image;
    private Long status;
}
