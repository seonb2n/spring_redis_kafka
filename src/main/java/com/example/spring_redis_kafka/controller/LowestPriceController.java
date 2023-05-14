package com.example.spring_redis_kafka.controller;

import com.example.spring_redis_kafka.common.exception.NotFoundException;
import com.example.spring_redis_kafka.service.redis.LowestPriceService;
import com.example.spring_redis_kafka.vo.Keyword;
import com.example.spring_redis_kafka.vo.Product;
import com.example.spring_redis_kafka.vo.ProductGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/redis")
public class LowestPriceController {

    @Autowired
    private LowestPriceService mlps;
    @GetMapping("/product")
    public Set GetZsetValue (String key){
        return mlps.GetZsetValue(key);
    }

    @GetMapping("/product1")
    public Set GetZsetValueWithStatus (String key){
        try {
            return mlps.GetZsetValueWithStatus(key);
        }
        catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/product2")
    public Set GetZsetValueUsingExController (String key) throws Exception {
        try {
            return mlps.GetZsetValueWithStatus(key);
        }
        catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    @GetMapping("/product3")
    public ResponseEntity<Set> GetZsetValueUsingExControllerWithSpecificException (String key) throws Exception {
        Set<String> mySet = new HashSet<>();
        try {
            mySet =  mlps.GetZsetValueWithSpecificException(key);
        }
        catch (NotFoundException ex) {
            throw new Exception(ex);
        }
        HttpHeaders responseHeaders = new HttpHeaders();

        return new ResponseEntity<Set>(mySet, responseHeaders, HttpStatus.OK);
    }


    @PutMapping("/product")
    public int SetNewProduct(@RequestBody Product newProduct) {
        return mlps.SetNewProduct(newProduct);

    }

    @PutMapping("/productGroup")
    public int SetNewProductGroup(@RequestBody ProductGroup newProductGrp) {
        return mlps.SetNewProductGrp(newProductGrp);
    }

    @PutMapping("/productGroupToKeyword")
    public int SetNewProductGrpToKeyword (String keyword, String prodGrpId, double score) {
        return mlps.SetNewProductGrpToKeyword(keyword, prodGrpId, score);
    }

    @GetMapping("/productPrice/lowest")
    public Keyword GetLowestPriceProductByKeyword (String keyword) {
        return mlps.GetLowestPriceProductByKeyword(keyword);
    }
}
