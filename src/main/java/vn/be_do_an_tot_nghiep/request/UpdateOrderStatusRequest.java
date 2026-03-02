package vn.be_do_an_tot_nghiep.request;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private Long orderId;
    private Long status;
}