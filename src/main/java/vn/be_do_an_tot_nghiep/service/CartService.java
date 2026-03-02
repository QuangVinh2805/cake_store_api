package vn.be_do_an_tot_nghiep.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.be_do_an_tot_nghiep.model.Cart;
import vn.be_do_an_tot_nghiep.model.Product;
import vn.be_do_an_tot_nghiep.model.ProductTaste;
import vn.be_do_an_tot_nghiep.model.User;
import vn.be_do_an_tot_nghiep.repository.CartRepository;
import vn.be_do_an_tot_nghiep.repository.ProductRepository;
import vn.be_do_an_tot_nghiep.repository.ProductTasteRepository;
import vn.be_do_an_tot_nghiep.repository.UserRepository;
import vn.be_do_an_tot_nghiep.request.CreateCartRequest;
import vn.be_do_an_tot_nghiep.request.UpdateCartRequest;
import vn.be_do_an_tot_nghiep.response.CartResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductTasteRepository productTasteRepository;


    private CartResponse mapToCartResponse(
            User user,
            Product product,
            ProductTaste productTaste,
            Cart cart
    ) {
        CartResponse response = new CartResponse();
        response.setUserName(user.getName());
        response.setProductName(product.getName() + " - " + productTaste.getTaste());
        response.setQuantity((long) cart.getQuantity());
        response.setUnitPrice(cart.getUnitPrice().longValue());
        response.setTotalPrice(cart.getTotalPrice().longValue());
        response.setImage(productTaste.getImage());
        response.setProductTasteId(productTaste.getId());
        return response;
    }




    public CartResponse addCart(CreateCartRequest request) {

        User user = userRepository.findByToken(request.getToken());
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        ProductTaste productTaste =
                productTasteRepository.findByProductTasteId(request.getProductTasteId());
        if (productTaste == null) {
            throw new RuntimeException("Không tìm thấy hương vị sản phẩm");
        }

        Product product =
                productRepository.findByProductId(productTaste.getProductId());
        if (product == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        Cart cart = cartRepository.findByUserIdAndProductTasteId(
                user.getId(), productTaste.getId()
        );

        if (cart == null) {
            cart = new Cart();
            cart.setUserId(user.getId());
            cart.setProductTasteId(productTaste.getId());
            cart.setQuantity(request.getQuantity());
            cart.setUnitPrice(productTaste.getPrice());
        } else {
            cart.setQuantity(cart.getQuantity() + request.getQuantity());
        }

        cart.setTotalPrice(cart.getUnitPrice() * cart.getQuantity());

        cartRepository.save(cart);

        return mapToCartResponse(user, product, productTaste, cart);
    }


    public CartResponse updateCart(
            String token,
            Long productTasteId,
            UpdateCartRequest request
    ) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        Cart cart = cartRepository.findByUserIdAndProductTasteId(
                user.getId(), productTasteId
        );
        if (cart == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        ProductTaste productTaste =
                productTasteRepository.findByProductTasteId(productTasteId);
        if (productTaste == null) {
            throw new RuntimeException("Hương vị không tồn tại");
        }

        Product product =
                productRepository.findByProductId(productTaste.getProductId());
        if (product == null) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        cart.setQuantity(request.getQuantity());
        cart.setTotalPrice(cart.getUnitPrice() * cart.getQuantity());

        cartRepository.save(cart);

        return mapToCartResponse(user, product, productTaste, cart);
    }



    public List<CartResponse> getCartByToken(String token) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        List<Cart> carts = cartRepository.findByUserId(user.getId());
        List<CartResponse> responses = new ArrayList<>();

        for (Cart cart : carts) {

            ProductTaste productTaste =
                    productTasteRepository.findByProductTasteId(cart.getProductTasteId());
            if (productTaste == null) continue;

            Product product =
                    productRepository.findByProductId(productTaste.getProductId());
            if (product == null) continue;

            responses.add(
                    mapToCartResponse(user, product, productTaste, cart)
            );
        }

        return responses;
    }


    public void deleteCartByToken(String token, Long productTasteId) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        Cart cart = cartRepository.findByUserIdAndProductTasteId(
                user.getId(), productTasteId
        );
        if (cart == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        cartRepository.delete(cart);
    }


    public void deleteAllCartByToken(String token) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new RuntimeException("Token không hợp lệ");
        }

        cartRepository.deleteByUserId(user.getId());
    }




}
