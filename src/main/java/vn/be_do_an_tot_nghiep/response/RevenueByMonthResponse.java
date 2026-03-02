package vn.be_do_an_tot_nghiep.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class RevenueByMonthResponse {

    private Integer year;
    private Integer month;
    private BigDecimal totalRevenue;

    // ⚠️ BẮT BUỘC constructor này
    public RevenueByMonthResponse(
            Integer year,
            Integer month,
            BigDecimal totalRevenue
    ) {
        this.year = year;
        this.month = month;
        this.totalRevenue = totalRevenue;
    }
}
