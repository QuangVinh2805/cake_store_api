package vn.be_do_an_tot_nghiep.response;

import lombok.Data;
import vn.be_do_an_tot_nghiep.model.ProductTaste;
import vn.be_do_an_tot_nghiep.model.Review;
import vn.be_do_an_tot_nghiep.model.User;

import java.util.Date;


@Data
public class ReviewResponse {
    private String userName;
    private String avatar;
    private String comment;
    private Long rate;
    private String image;
    private Date createdAt;
    private String productTasteName;

    public ReviewResponse(Review r, User user, ProductTaste productTaste) {
        this.userName = user.getName();
        this.avatar = user.getAvatar();
        this.comment = r.getComment();
        this.rate = r.getRate();
        this.image = r.getImage();
        this.createdAt = r.getCreatedAt();
        this.productTasteName = productTaste.getTaste();
    }
}
