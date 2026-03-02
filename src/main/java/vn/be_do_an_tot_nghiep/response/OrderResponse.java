package vn.be_do_an_tot_nghiep.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private String userName;
    private Long totalPrice;
    private String address;
    private Long status;
    private String email;
    private String phone;
    private Long isCheckout;
    private Date createdAt;
    private String paymentMethod;
    private List<OrderDetailResponse> details;
}

