package vn.be_do_an_tot_nghiep.request;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private String token;
    private String address;
}
