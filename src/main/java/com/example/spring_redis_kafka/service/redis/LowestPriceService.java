package com.example.spring_redis_kafka.service.redis;


import com.example.spring_redis_kafka.vo.Keyword;
import com.example.spring_redis_kafka.vo.Product;
import com.example.spring_redis_kafka.vo.ProductGroup;

import java.util.Set;

public interface LowestPriceService {
    Set GetZsetValue(String key);

    Set GetZsetValueWithStatus(String key) throws Exception;

    Set GetZsetValueWithSpecificException(String key) throws Exception;

    int SetNewProduct(Product newProduct);

    int SetNewProductGrp(ProductGroup newProductGrp);

    int SetNewProductGrpToKeyword(String keyword, String prodGrpId, double score);

    Keyword GetLowestPriceProductByKeyword(String keyword);

}
