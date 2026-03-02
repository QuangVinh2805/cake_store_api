package vn.be_do_an_tot_nghiep.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusSummaryResponse {
    private long deliveredOrders;
    private long canceledOrders;
    private long totalOrders;
}
