package com.tarun.nest.service;

import com.tarun.nest.dto.AddProduct;
import com.tarun.nest.entity.Product;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final Path uploadDirectory =
            Paths.get("uploads/products");

    public Product addProduct(
            AddProduct request,
            User vendor
    ) throws IOException {

        Files.createDirectories(uploadDirectory);

        MultipartFile image = request.getImage();

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Product image is required"
            );
        }

        String originalFileName = image.getOriginalFilename();

        String extension = "";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        String fileName = UUID.randomUUID() + extension;

        Path filePath = uploadDirectory.resolve(fileName);

        Files.copy(
                image.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        Product product = new Product();

        product.setName(request.getName());
        product.setImageUrl(fileName);
        product.setCategory(request.getCategory());
        product.setStatus(request.getStatus());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        // Very important:
        // Attach the logged-in vendor
        product.setVendor(vendor);

        return productRepository.save(product);
    }
}