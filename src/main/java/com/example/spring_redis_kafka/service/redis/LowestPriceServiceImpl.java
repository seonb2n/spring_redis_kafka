package com.example.spring_redis_kafka.service.redis;

import com.example.spring_redis_kafka.common.exception.NotFoundException;
import com.example.spring_redis_kafka.vo.Keyword;
import com.example.spring_redis_kafka.vo.Product;
import com.example.spring_redis_kafka.vo.ProductGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService {

    private final RedisTemplate myRedisServer;

    /**
     * zSet 으로 구성된 key 내부에서 가장 저렴한 10개의 값을 가져온다.
     * @param key
     * @return
     */
    public Set GetZsetValue(String key) {
        Set myTempSet = new HashSet();
        myTempSet = myRedisServer.opsForZSet().rangeWithScores(key, 0, 9);
        return myTempSet;
    }

    /**
     * zSet 으로 구성된 key 내부에서 가장 저렴한 10개의 값을 가져오되, 해당 key 가 비어 있는 경우 Exception을 발생시킨다.
     * @param key
     * @return
     * @throws Exception
     */
    public Set GetZsetValueWithStatus(String key) throws Exception {
        Set myTempSet = new HashSet();
        myTempSet = myRedisServer.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size() < 1) {
            throw new Exception("The Key doesn't have any member");
        }
        return myTempSet;
    }

    /**
     * zSet 으로 구성된 key 내부에서 가장 저렴한 10개의 값을 가져오되, 해당 key 가 비어 있는 경우 NotFoundException을 발생시킨다.
     * @param key
     * @return
     * @throws Exception
     */
    public Set GetZsetValueWithSpecificException(String key) throws Exception {
        Set myTempSet = new HashSet();
        myTempSet = myRedisServer.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size() < 1) {
            throw new NotFoundException("The Key doesn't exist in redis", HttpStatus.NOT_FOUND);
        }
        return myTempSet;
    }

    /**
     * prodGroupId 를 key 로 하는 zSet 에 새로운 Product 를 추가한다.
     * @param newProduct
     * @return
     */
    public int SetNewProduct(Product newProduct) {
        int rank = 0;
        myRedisServer.opsForZSet().add(newProduct.getProdGrpId(), newProduct.getProductId(), newProduct.getPrice());
        rank = myRedisServer.opsForZSet().rank(newProduct.getProdGrpId(), newProduct.getProductId()).intValue();
        return rank;
    }

    /**
     * productGroupId 를 key 로 하는 새로운 zSet 을 생성하고 key 에 속한 멤버의 개수를 반환받는다.
     * @param newProductGrp
     * @return
     */
    public int SetNewProductGrp(ProductGroup newProductGrp) {

        List<Product> product = newProductGrp.getProductList();
        String productId = product.get(0).getProductId();
        double price = product.get(0).getPrice();
        myRedisServer.opsForZSet().add(newProductGrp.getProdGrpId(), productId, price);
        int productCnt = myRedisServer.opsForZSet().zCard(newProductGrp.getProdGrpId()).intValue();
        return productCnt;
    }

    /**
     * redis에서 key 로 된 객체를 삭제한다.
     * @param key
     */
    public void DeleteKey(String key) {
        myRedisServer.delete(key);
    }

    /**
     * keyword 내에 새로운 ProductGroup 을 추가한다. productGroupId 가 member 이고 score 는 keyword 와의 연관도이다.
     * 이후, 해당 객체의 순위를 반환한다.
     * @param keyword
     * @param prodGrpId
     * @param score
     * @return
     */
    public int SetNewProductGrpToKeyword(String keyword, String prodGrpId, double score) {
        myRedisServer.opsForZSet().add(keyword, prodGrpId, score);
        return myRedisServer.opsForZSet().rank(keyword, prodGrpId).intValue();
    }

    /**
     * keyword 내에 존재하는 상품 그룹 마다 값이 가장 저렴한 10개의 상품을 담고 있도록 하는 keyword 객체 조회
     * @param keyword
     * @return
     */
    public Keyword GetLowestPriceProductByKeyword(String keyword) {
        Keyword returnInfo = new Keyword();
        List<ProductGroup> tempProdGrp = new ArrayList<>();
        // keyword 를 통해 ProductGroup 가져오기 (10개)
        tempProdGrp = GetProdGrpUsingKeyword(keyword);

        // 가져온 정보들을 Return 할 Object 에 넣기
        returnInfo.setKeyword(keyword);
        returnInfo.setProductGrpList(tempProdGrp);
        // 해당 Object return
        return returnInfo;
    }

    /**
     * keyword 로 가져온 모든 productGroup 내에서, 가장 값이 싼 10개의 product를 담고 있는 productGroupList 조회
     * @param keyword
     * @return
     */
    public List<ProductGroup> GetProdGrpUsingKeyword(String keyword) {

        List<ProductGroup> returnInfo = new ArrayList<>();

        // input 받은 keyword 로 productGrpId를 조회
        // 연관도가 큰 productGroup 을 가져와야 하게 띠문애 reverseRange 로 productGroup 리스트를 가져온다.
        List<String> prodGrpIdList = new ArrayList<>();
        prodGrpIdList = List.copyOf(myRedisServer.opsForZSet().reverseRange(keyword, 0, 9));
        //Product tempProduct = new Product();
        List<Product> tempProdList = new ArrayList<>();

        //10개 prodGrpId로 loop
        for (final String prodGrpId : prodGrpIdList) {
            // Loop 타면서 ProductGrpID 로 Product:price 가져오기 (10개)

            ProductGroup tempProdGrp = new ProductGroup();

            Set prodAndPriceList = new HashSet();
            prodAndPriceList = myRedisServer.opsForZSet().rangeWithScores(prodGrpId, 0, 9);
            Iterator<Object> prodPricObj = prodAndPriceList.iterator();

            // loop 타면서 product obj에 bind (10개)
            while (prodPricObj.hasNext()) {
                ObjectMapper objMapper = new ObjectMapper();
                // {"value":00-10111-}, {"score":11000}
                Map<String, Object> prodPriceMap = objMapper.convertValue(prodPricObj.next(), Map.class);
                Product tempProduct = new Product();
                // Product Obj bind
                tempProduct.setProductId(prodPriceMap.get("value").toString()); // prod_id
                tempProduct.setPrice(Double.valueOf(prodPriceMap.get("score").toString()).intValue()); //es 검색된 score
                tempProduct.setProdGrpId(prodGrpId);

                tempProdList.add(tempProduct);
            }
            // 10개 product price 입력완료
            tempProdGrp.setProdGrpId(prodGrpId);
            tempProdGrp.setProductList(tempProdList);
            returnInfo.add(tempProdGrp);
        }

        return returnInfo;
    }

}
