package vn.be_do_an_tot_nghiep.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.be_do_an_tot_nghiep.model.Category;
import vn.be_do_an_tot_nghiep.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    // GET all
    @GetMapping("/getAllPaging")
    public Page<Category> getAllCategoriesPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return categoryService.getAllPaging(page, size);
    }

    @GetMapping("/getAll")
    public List<Category> getAllCategoriesByStatus() {
        return categoryService.getAllByStatus();
    }


    @GetMapping("/getAllCate")
    public List<Category> getAllCategories() {
        return categoryService.getAll();
    }




    // GET by id
    @GetMapping("/getById")
    public ResponseEntity<Category> getCategoryById(@RequestParam Long id) {
        try {
            return ResponseEntity.ok(categoryService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public Category createCategory(@RequestBody Category category) {
        return categoryService.create(category);
    }

    @PutMapping("/update")
    public ResponseEntity<Category> updateCategory(@RequestParam Long id, @RequestBody Category category) {
        try {
            return ResponseEntity.ok(categoryService.update(id, category));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestParam Long id){
        categoryService.changeStatus(id);
        return ResponseEntity.ok("Đổi trạng thái thành công!");
    }
}
