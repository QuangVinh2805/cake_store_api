package vn.be_do_an_tot_nghiep.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.be_do_an_tot_nghiep.config.VnpayConfig;
import vn.be_do_an_tot_nghiep.model.*;
import vn.be_do_an_tot_nghiep.repository.*;
import vn.be_do_an_tot_nghiep.request.CheckoutRequest;
import vn.be_do_an_tot_nghiep.request.CreateVnpayOrderRequest;
import vn.be_do_an_tot_nghiep.request.UpdateOrderStatusRequest;
import vn.be_do_an_tot_nghiep.response.*;
import vn.be_do_an_tot_nghiep.util.JwtUtil;
import vn.be_do_an_tot_nghiep.util.VnpayUtil;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderService {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    TransactionsRepository transactionsRepository;

    @Autowired
    ProductTasteRepository productTasteRepository;

    @Autowired
    private JavaMailSender emailSender;

    public CreateOrderResponse checkout(CheckoutRequest request) {

        String token = request.getToken();

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token hết hạn,vui lòng đăng nhập lại!");
        }

        String phone = jwtUtil.getPhoneFromToken(token);

        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        Long userId = user.getId();

        List<Cart> carts = cartRepository.findByUserId(userId);
        if (carts.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        long totalPrice = 0;
        for (Cart cart : carts) {
            totalPrice += cart.getTotalPrice();
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(request.getAddress());
        order.setTotalPrice(totalPrice);
        order.setStatus(0L); // chuẩn bị
        order.setIsCheckout(0L);
        order.setCreatedAt(new Date());
        orderRepository.save(order);

        for (Cart cart : carts) {
            ProductTaste productTaste =
                    productTasteRepository.findByProductTasteId(cart.getProductTasteId());

            if (productTaste == null) {
                throw new RuntimeException("Sản phẩm không tồn tại");
            }

            if (productTaste.getQuantity() < cart.getQuantity()) {
                throw new RuntimeException(
                        "Sản phẩm " + productTaste.getTaste() + " không đủ số lượng"
                );
            }
            productTaste.setQuantity(
                    productTaste.getQuantity() - cart.getQuantity()
            );
            productTasteRepository.save(productTaste);
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getId());
            detail.setProductTasteId(cart.getProductTasteId());
            detail.setQuantity(cart.getQuantity());
            detail.setTotalPrice(cart.getTotalPrice());
            detail.setCreatedAt(new Date());
            orderDetailRepository.save(detail);

        }

        cartRepository.deleteByUserId(userId);

        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(order.getId());
        response.setUserName(user.getName());

        return response;
    }

    public VnpayResponse createVnpayPayment(
            String token,
            CreateVnpayOrderRequest request
    ) throws UnsupportedEncodingException {

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token hết hạn");
        }

        // 1️⃣ lấy user từ token
        String phone = jwtUtil.getPhoneFromToken(token);
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // 2️⃣ lấy order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("Đơn hàng không thuộc người dùng");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("Đơn hàng không hợp lệ để thanh toán");
        }

        // 3️⃣ config VNPay
        String tmnCode = VnpayConfig.TMN_CODE;
        String hashSecret = VnpayConfig.HASH_SECRET;

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(order.getTotalPrice() * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(System.currentTimeMillis()));
        params.put("vnp_OrderInfo", user.getId() + "|" + order.getId());
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", VnpayConfig.RETURN_URL);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put(
                "vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        // 4️⃣ build query
        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) query.append('&');
            }
        }

        String queryUrl = query.toString();
        String secureHash = VnpayUtil.hmacSHA512(hashSecret, queryUrl);

        String paymentUrl =
                VnpayConfig.PAY_URL + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;

        return new VnpayResponse(paymentUrl);
    }

    /* ================= SUCCESS PAYMENT ================= */
    public void processSuccessPayment(
            Long userId,
            Long orderId,
            String paymentMethod
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        if (order.getStatus() != 0) {
            return; // tránh callback trùng
        }

        // 1️⃣ lưu transaction
        Transactions transaction = new Transactions();
        transaction.setUserId(userId);
        transaction.setOrderId(orderId);
        transaction.setAmount(order.getTotalPrice());
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus("SUCCESS");
        transactionsRepository.save(transaction);

        // 2️⃣ cập nhật order
        order.setStatus(0L);
        order.setIsCheckout(1L);
        order.setCreatedAt(new Date());
        orderRepository.save(order);
    }


    @Transactional
    public void updateStatus(UpdateOrderStatusRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Long newStatus = request.getStatus();
        Long oldStatus = order.getStatus();

        // ===== HOÀN KHO NẾU HỦY ĐƠN =====
        if (newStatus == 3L && oldStatus != 3L) {

            List<OrderDetail> details =
                    orderDetailRepository.findByOrderId(order.getId());

            for (OrderDetail detail : details) {

                ProductTaste productTaste = productTasteRepository
                        .findById(detail.getProductTasteId())
                        .orElseThrow(() ->
                                new RuntimeException("Product taste không tồn tại"));

                // hoàn lại số lượng
                productTaste.setQuantity(
                        productTaste.getQuantity() + detail.getQuantity()
                );

                productTasteRepository.save(productTaste);
            }
        }

        // ===== NẾU ĐÃ GIAO → ĐÁNH DẤU THANH TOÁN =====
        if (newStatus == 2L) {
            order.setIsCheckout(1L);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(new Date());

        // ===== EMAIL NỘI DUNG THEO STATUS =====
        String subject = "Thông báo trạng thái đơn hàng";
        String content;

        switch (newStatus.intValue()) {
            case 0:
                content = "Xin chào " + user.getName() + ",\n\n"
                        + "Đơn hàng của bạn đang được chuẩn bị.\n"
                        + "Cảm ơn bạn đã mua sắm tại cửa hàng!";
                break;

            case 1:
                content = "Xin chào " + user.getName() + ",\n\n"
                        + "Đơn hàng của bạn đang được giao.\n"
                        + "Vui lòng chú ý điện thoại để nhận hàng.";
                break;

            case 2:
                content = "Xin chào " + user.getName() + ",\n\n"
                        + "Đơn hàng đã được giao thành công.\n"
                        + "Cảm ơn bạn đã ủng hộ cửa hàng!";
                break;

            case 3:
                content = "Xin chào " + user.getName() + ",\n\n"
                        + "Đơn hàng của bạn đã được hủy.\n"
                        + "Số lượng sản phẩm đã được hoàn lại kho.\n"
                        + "Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.";
                break;

            default:
                content = "Trạng thái đơn hàng của bạn đã được cập nhật.";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("vinhquangngo2805@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(content);

        emailSender.send(message);

        orderRepository.save(order);
    }




    public Page<OrderResponse> getOrdersByToken(String token, int page, int size) {

        if (jwtUtil.isTokenExpired(token)) {
            throw new RuntimeException("Token hết hạn");
        }

        String phone = jwtUtil.getPhoneFromToken(token);
        User user = userRepository.findByPhone(phone);

        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);

        return orders.map(this::mapToResponse);
    }


    public Page<OrderResponse> getAllOrders(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(this::mapToResponse);
    }



    private OrderResponse mapToResponse(Order order) {

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Transactions transactions =
                transactionsRepository.findByOrderId(order.getId());

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setUserName(user.getName());
        response.setTotalPrice(order.getTotalPrice());
        response.setAddress(order.getAddress());
        response.setStatus(order.getStatus());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setIsCheckout(order.getIsCheckout());
        response.setCreatedAt(order.getCreatedAt());

        // payment method
        if (transactions != null && transactions.getPaymentMethod() != null) {
            response.setPaymentMethod(transactions.getPaymentMethod());
        } else {
            response.setPaymentMethod("Tiền mặt");
        }

        // order details
        List<OrderDetail> details =
                orderDetailRepository.findByOrderId(order.getId());

        List<OrderDetailResponse> detailResponses = new ArrayList<>();

        for (OrderDetail detail : details) {

            ProductTaste productTaste =
                    productTasteRepository.findByProductTasteId(detail.getProductTasteId());

            OrderDetailResponse d = new OrderDetailResponse();
            d.setTaste(productTaste.getTaste());
            d.setImage(productTaste.getImage());
            d.setQuantity(detail.getQuantity());
            d.setTotalPrice(productTaste.getPrice());

            detailResponses.add(d);
        }

        response.setDetails(detailResponses);

        return response;
    }



    public List<RevenueByMonthResponse> getRevenueByMonth() {
        return orderRepository.getRevenueByMonthRaw()
                .stream()
                .map(r -> new RevenueByMonthResponse(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        (BigDecimal) r[2]
                ))
                .toList();
    }


    public List<OrderStatusByMonthResponse> getOrderStatusByMonth() {

        List<Object[]> rows = orderRepository.getOrderStatusByMonth();

        return rows.stream()
                .map(r -> new OrderStatusByMonthResponse(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        r[2] == null ? 0L : ((Number) r[2]).longValue(),
                        r[3] == null ? 0L : ((Number) r[3]).longValue()
                ))
                .toList();
    }

    public OrderStatusSummaryResponse getOrderStatusSummary() {

        Object[] row = (Object[]) orderRepository.getOrderStatusSummary();

        long delivered = row[0] == null ? 0L : ((Number) row[0]).longValue();
        long canceled  = row[1] == null ? 0L : ((Number) row[1]).longValue();

        return new OrderStatusSummaryResponse(
                delivered,
                canceled,
                delivered + canceled
        );
    }

    public Page<OrderResponse> searchByEmailOrPhone(
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders =
                orderRepository.searchByEmailOrPhone(keyword.trim(), pageable);

        return orders.map(this::mapToResponse);
    }


    public Page<OrderResponse> filterByStatus(
            Integer status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders =
                orderRepository.filterByStatus(status, pageable);

        return orders.map(this::mapToResponse);
    }


    public Page<OrderResponse> filterByCheckout(
            Integer isCheckout,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders =
                orderRepository.filterByCheckout(isCheckout, pageable);

        return orders.map(this::mapToResponse);
    }


    public Page<OrderResponse> filterByDateRange(
            Date fromDate,
            Date toDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders =
                orderRepository.filterByDateRange(fromDate, toDate, pageable);

        return orders.map(this::mapToResponse);
    }




}


