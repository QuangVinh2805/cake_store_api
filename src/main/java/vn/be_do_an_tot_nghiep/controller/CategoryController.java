package vn.be_do_an_tot_nghiep.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/getAll")
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

    // POST create
    @PostMapping("/create")
    public Category createCategory(@RequestBody Category category) {
        return categoryService.create(category);
    }

    // PUT update
    @PutMapping("/update")
    public ResponseEntity<Category> updateCategory(@RequestParam Long id, @RequestBody Category category) {
        try {
            return ResponseEntity.ok(categoryService.update(id, category));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCategory(@RequestParam Long id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
