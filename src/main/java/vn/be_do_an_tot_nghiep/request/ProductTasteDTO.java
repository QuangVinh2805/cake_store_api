package vn.be_do_an_tot_nghiep.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTasteDTO {
    private String taste;
    private Long price;
    private Long quantity;
    private String secondDes;
}

