package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllReviewResponse {
    private Long reviewId;

    private String userName;
    private String userAvatar;

    private String productName;
    private String tasteName;

    private String comment;
    private Integer rate;

    private Date createdAt;
}
