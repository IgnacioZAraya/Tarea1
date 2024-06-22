package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepsository;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {
    @Autowired
    private ProductRepository ProductRepository;

    @Autowired
    private CategoryRepsository CategoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public List<Product> getAllProducts() {return ProductRepository.findAll();}

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        Optional<Category> optionalCategory = CategoryRepository.findByName(product.getCategory().getName());

        if (optionalCategory.isEmpty()){
            return null;
        }

        product.setCategory(optionalCategory.get());
        Product savedProduct = ProductRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @GetMapping(value = "/{id}")
    public Product getProductById(@PathVariable Long id) {
        return ProductRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @GetMapping("/filterByCharacter/{name}")
    public List<Product> getProductByCharacter(@PathVariable String name) {
        return ProductRepository.findProductsWithCharacterInName(name);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ProductRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setStockSize(product.getStockSize());

                    Optional<Category> optionalCategory = CategoryRepository.findByName(product.getCategory().getName());

                    if (optionalCategory.isEmpty()){
                        return null;
                    }

                    existingProduct.setCategory(optionalCategory.get());

                    return ProductRepository.save(existingProduct);
                })
                .orElseGet(() -> {
                    product.setId(id);
                    return ProductRepository.save(product);
                });
    }

    @DeleteMapping( "/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteProduct(@PathVariable Long id) {
        ProductRepository.deleteById(id);
    }
}
