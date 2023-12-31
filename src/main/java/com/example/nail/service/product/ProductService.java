package com.example.nail.service.product;

import com.example.nail.domain.File;
import com.example.nail.domain.Product;
import com.example.nail.exception.ResourceNotFoundException;
import com.example.nail.repository.FileRepository;
import com.example.nail.repository.ProductRepository;
import com.example.nail.service.file.FileResponse;
import com.example.nail.service.product.request.ProductEditRequest;
import com.example.nail.service.product.request.ProductSaveRequest;
import com.example.nail.service.product.response.ProductEditResponse;
import com.example.nail.service.product.response.ProductListResponse;
import com.example.nail.service.request.SelectOptionRequest;
import com.example.nail.service.response.SelectOptionResponse;
import com.example.nail.util.AppMessage;
import com.example.nail.util.AppUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final FileRepository fileRepository;

    public ResponseEntity<?> createProduct(ProductSaveRequest request){
        var product = AppUtil.mapper.map(request, Product.class);
        product.setDeleted(false);
        productRepository.save(product);
        var images = fileRepository.findAllById(request.getImages().stream().map(SelectOptionRequest::getId).collect(Collectors.toList()));
        for (var image: images) {
            image.setProduct(product);
        }
        fileRepository.saveAll(images);
        return ResponseEntity.ok("Sản phẩm đã được tạo thành công.");

    }
    public Page<ProductListResponse> showListProduct(String search, Pageable pageable, BigDecimal min, BigDecimal max){

        return productRepository.searchAllByService(search,pageable,min,max)
                .map(product -> ProductListResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .poster(String.valueOf(product.getPoster().getFileUrl()))
                        .images(String.valueOf(product.getImages()))
                        .deleted(product.getDeleted())
                        .build());
    }
    @Transactional
    public void deleteById(Long id) {
        Product product = findById(id);
        fileRepository.deleteAllByProductId(id);
        product.setDeleted(true);
        productRepository.save(product);
    }
    public Product findById(Long id) {
        //de tai su dung
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException
                        (String.format(AppMessage.ID_NOT_FOUND, "Product", id)));
    }
    public ProductEditResponse findByIdProduct(Long id) {
        var product = findById(id);
        var result = AppUtil.mapper.map(product, ProductEditResponse.class);
        result.setPoster(product.getPoster().getFileUrl());
        result.setIdPoster(product.getPoster().getId());
        List<String> images = product.getImages()
                .stream()
                .map(File::getFileUrl)
                .collect(Collectors.toList());

        result.setImages(images);
        List<String> idImages = product.getImages().stream().map(File::getId).toList();
        result.setIdImages(idImages);
        return result;
    }
    @Transactional
    public ResponseEntity<?>  update(ProductEditRequest request, Long id) throws Exception {
        var productDB = findById(id);
            productDB.getImages().forEach(e -> {
            e.setProduct(null);
            fileRepository.save(e);
        });
        var images = fileRepository.findAllById(request.getImages().stream().map(SelectOptionRequest::getId).collect(Collectors.toList()));
        for (var image: images) {
            image.setProduct(productDB);
        }
        productDB.setName(request.getName());
        productDB.setDescription(request.getDescription());
        productDB.setPrice(new BigDecimal(request.getPrice()));
        if(request.getPoster() != null && request.getPoster().getId() !=null){
            productDB.setPoster(File.builder().id(request.getPoster().getId()).build());
        }

        fileRepository.saveAll(images);
        productRepository.save(productDB);
        return ResponseEntity.ok("Sản phẩm đã được sửa thành công.");

    }
    public List<SelectOptionResponse> findAll() {
        return productRepository.findAllByDeletedFalse()
                .stream().map(product -> new SelectOptionResponse(product.getId()
                        .toString(), product.getName())).collect(Collectors.toList());
    }
}
