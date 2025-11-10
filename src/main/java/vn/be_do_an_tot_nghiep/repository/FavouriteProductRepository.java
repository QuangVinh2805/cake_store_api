package vn.be_do_an_tot_nghiep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.be_do_an_tot_nghiep.model.FavouriteProduct;

import java.util.List;

public interface FavouriteProductRepository extends JpaRepository<FavouriteProduct,Long> {
    FavouriteProduct findByUser_IdAndProduct_Id(Long userId, Long productId);
    List<FavouriteProduct> findByUser_Id(Long userId);
}
