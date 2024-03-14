package com.example.pricecompareredis.controller;

import com.example.pricecompareredis.service.LowestPriceService;
import com.example.pricecompareredis.vo.Keyword;
import com.example.pricecompareredis.vo.NotFoundException;
import com.example.pricecompareredis.vo.Product;
import com.example.pricecompareredis.vo.ProductGrp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {

    private final LowestPriceService lowestPriceService;


    @GetMapping("/product")
    public Set getZsetValue(String key) {
        return lowestPriceService.getZsetValue(key);
    }

    @GetMapping("/product1")
    public Set getZsetValueWithStatus(String key) {
        try{
            return lowestPriceService.getZsetValueWithStatus(key);
        }
        catch (Exception e){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/product2")
    public Set getZsetValueWithExController(String key) throws Exception {
        try{
            return lowestPriceService.getZsetValueWithStatus(key);
        }
        catch (Exception e){
            throw new Exception(e);
        }
    }

    @GetMapping("/product3")
    public ResponseEntity<Set> getZsetValueWithExControllerWithSpecificException(String key) throws Exception {
        Set<String> mySet = new HashSet<>();
        try{
            mySet = lowestPriceService.getZsetValueWithSpecificException(key);
        }
        catch (NotFoundException e){
            throw new Exception(e);
        }
        HttpHeaders responseHeaders = new HttpHeaders();

        return new ResponseEntity<Set>(mySet, responseHeaders, HttpStatus.OK);
    }



    @PutMapping("/product")
    public int setNewProduct(@RequestBody Product newProduct) {
        return lowestPriceService.setNewProduct(newProduct);
    }

    @PutMapping("/productGroup")
    public int setNewProduct(@RequestBody ProductGrp newProductGrp) {
        return lowestPriceService.setNewProductGrp(newProductGrp);
    }

    @PutMapping("/productGroupToKeyword")
    public int setNewProductGrpToKeyword(String keyword, String prodGrpId, double score) {
        return lowestPriceService.setNewProductGrpToKeyword(keyword, prodGrpId, score);
    }

    @GetMapping("/productPrice/lowest")
    public Keyword getLowestPriceProductByKeyword(String keyword) {
        return lowestPriceService.getLowestPriceProductByKeyword(keyword);
    }
}
