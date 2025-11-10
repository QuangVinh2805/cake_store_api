package vn.be_do_an_tot_nghiep.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private Long price;
    private String firstDes;
    private Long categoryId; // chỉ cần id
}
