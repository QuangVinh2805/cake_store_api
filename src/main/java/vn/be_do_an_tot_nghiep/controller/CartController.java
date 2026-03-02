package vn.be_do_an_tot_nghiep.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.be_do_an_tot_nghiep.request.CreateCartRequest;
import vn.be_do_an_tot_nghiep.request.UpdateCartRequest;
import vn.be_do_an_tot_nghiep.response.CartResponse;
import vn.be_do_an_tot_nghiep.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {


    @Autowired
    CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addCart(
            @RequestBody CreateCartRequest request
    ) {
        return ResponseEntity.ok(cartService.addCart(request));
    }

    @PutMapping("/update/{productTasteId}")
    public ResponseEntity<CartResponse> updateCart(
            @RequestParam String token,
            @PathVariable Long productTasteId,
            @RequestBody UpdateCartRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateCart(token, productTasteId, request)
        );
    }

    @GetMapping("/getCartByToken")
    public ResponseEntity<List<CartResponse>> getCart(@RequestParam String token) {
        return ResponseEntity.ok(cartService.getCartByToken(token));
    }

    @DeleteMapping("/delete/{productTasteId}")
    public ResponseEntity<?> deleteCart(
            @RequestParam String token,
            @PathVariable Long productTasteId) {

        cartService.deleteCartByToken(token, productTasteId);
        return ResponseEntity.ok("Xóa sản phẩm khỏi giỏ hàng thành công");
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllCart(@RequestParam String token) {
        cartService.deleteAllCartByToken(token);
        return ResponseEntity.ok("Đã xóa toàn bộ giỏ hàng");
    }
}

