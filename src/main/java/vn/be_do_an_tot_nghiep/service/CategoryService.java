package vn.be_do_an_tot_nghiep.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.be_do_an_tot_nghiep.model.Category;
import vn.be_do_an_tot_nghiep.model.User;
import vn.be_do_an_tot_nghiep.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    public Page<Category> getAllPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findAll(pageable);
    }

    public List<Category> getAllByStatus() {
        return categoryRepository.getAll();
    }


    public List<Category> getAll() {
        return categoryRepository.findAll();
    }


    public Category getById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new RuntimeException("Category not found with id " + id);
        }
        return category;
    }

    public Category create(Category category) {

        // 1. Validate name
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên danh mục không được để trống");
        }

        // 2. Check trùng tên
        if (categoryRepository.existsByNameIgnoreCase(category.getName().trim())) {
            throw new RuntimeException("Danh mục đã tồn tại");
        }

        // 3. Chuẩn hóa data
        category.setName(category.getName().trim());
        category.setStatus(1L); // mặc định hiện

        return categoryRepository.save(category);
    }


    public Category update(Long id, Category categoryDetails) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id " + id));

        // 1. Validate name
        if (categoryDetails.getName() == null || categoryDetails.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên danh mục không được để trống");
        }

        // 2. Check trùng tên (trừ chính nó)
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
                categoryDetails.getName().trim(),
                id
        )) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        // 3. Update
        category.setName(categoryDetails.getName().trim());

        return categoryRepository.save(category);
    }

    public void changeStatus(Long id){
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            throw new RuntimeException("Category not found with id " + id);
        }

        category.setStatus(category.getStatus() == 1L ? 0L : 1L);
        categoryRepository.save(category);
    }
}
