package vn.be_do_an_tot_nghiep.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.be_do_an_tot_nghiep.request.CheckoutRequest;
import vn.be_do_an_tot_nghiep.request.CreateVnpayOrderRequest;
import vn.be_do_an_tot_nghiep.request.UpdateOrderStatusRequest;
import vn.be_do_an_tot_nghiep.response.*;
import vn.be_do_an_tot_nghiep.service.OrderService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    /* ================= CHECKOUT ================= */
    @PostMapping("/checkout")
    public CreateOrderResponse checkout(@RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    @PostMapping("/vnpay/create")
    public VnpayResponse createVnpay(
            @RequestParam String token,
            @RequestBody CreateVnpayOrderRequest request
    ) throws UnsupportedEncodingException {
        return orderService.createVnpayPayment(token, request);
    }

    @GetMapping("/vnpay/callback")
    public void vnpayCallback(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            String orderInfo = params.get("vnp_OrderInfo");
            String[] info = orderInfo.split("\\|");

            Long userId = Long.parseLong(info[0]);
            Long orderId = Long.parseLong(info[1]);

            orderService.processSuccessPayment(userId, orderId, "VNPAY");

            response.sendRedirect("http://localhost:3000/my-orders?status=success");
        } else {
            response.sendRedirect("http://localhost:3000/my-orders?status=error");
        }
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<?> updateStatus(
            @RequestBody UpdateOrderStatusRequest request
    ) {
        orderService.updateStatus(request);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    @GetMapping("/myOrders")
    public ResponseEntity<Page<OrderResponse>> getByToken(
            @RequestParam String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(orderService.getOrdersByToken(token, page, size));
    }


    @GetMapping("/getAll")
    public ResponseEntity<Page<OrderResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }


    @GetMapping("/revenue-by-month")
    public ResponseEntity<List<RevenueByMonthResponse>> revenueByMonth() {
        return ResponseEntity.ok(orderService.getRevenueByMonth());
    }

    @GetMapping("/order-status-by-month")
    public ResponseEntity<List<OrderStatusByMonthResponse>> orderStatusByMonth() {
        return ResponseEntity.ok(orderService.getOrderStatusByMonth());
    }

    @GetMapping("/order-status-summary")
    public ResponseEntity<OrderStatusSummaryResponse> orderStatusSummary() {
        return ResponseEntity.ok(orderService.getOrderStatusSummary());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderResponse>> searchByEmailOrPhone(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                orderService.searchByEmailOrPhone(keyword, page, size)
        );
    }


    @GetMapping("/filter/status")
    public ResponseEntity<Page<OrderResponse>> filterByStatus(
            @RequestParam Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                orderService.filterByStatus(status, page, size)
        );
    }


    @GetMapping("/filter/checkout")
    public ResponseEntity<Page<OrderResponse>> filterByCheckout(
            @RequestParam Integer isCheckout,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                orderService.filterByCheckout(isCheckout, page, size)
        );
    }

    @GetMapping("/filter/date")
    public ResponseEntity<Page<OrderResponse>> filterByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(
                orderService.filterByDateRange(from, to, page, size)
        );
    }



}
