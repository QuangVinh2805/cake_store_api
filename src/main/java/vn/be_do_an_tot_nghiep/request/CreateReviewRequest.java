package vn.be_do_an_tot_nghiep.request;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private Long productTasteId;
    private String comment;
    private Long rate;
}
