package com.example.pricecompareredis.service;

import com.example.pricecompareredis.vo.Keyword;
import com.example.pricecompareredis.vo.Product;
import com.example.pricecompareredis.vo.ProductGrp;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService{

    private final RedisTemplate redisTemplate;

    @Override
    public Set getZsetValue(String key) {
        Set myTempSet = new HashSet();
        myTempSet = redisTemplate.opsForZSet().rangeWithScores(key, 0, 9);
        return myTempSet;
    }

    @Override
    public int setNewProduct(Product newProduct) {
        int rank = 0;
        redisTemplate.opsForZSet().add(newProduct.getProdGrpId(), newProduct.getProductId(), newProduct.getPrice());
        rank = redisTemplate.opsForZSet().rank(newProduct.getProdGrpId(), newProduct.getProductId()).intValue();

        return rank;
    }

    @Override
    public int setNewProductGrp(ProductGrp newProductGrp) {
        List<Product> productList = newProductGrp.getProductList();
        String productId = productList.get(0).getProductId();
        int price = productList.get(0).getPrice();
        redisTemplate.opsForZSet().add(newProductGrp.getProdGrpId(), productId, price);
        int productCnt = redisTemplate.opsForZSet().zCard(newProductGrp.getProdGrpId()).intValue();
        return productCnt;
    }

    public int setNewProductGrpToKeyword(String keyword, String prodGrpId, double score) {
        redisTemplate.opsForZSet().add(keyword, prodGrpId, score);
        int rank = redisTemplate.opsForZSet().rank(keyword, prodGrpId).intValue();
        return rank;
    }

    @Override
    public Keyword getLowestPriceProductByKeyword(String keyword) {
        Keyword returnInfo = new Keyword();
        List<ProductGrp> tempProdGrp = new ArrayList<>();

        tempProdGrp = getProdGrpUsingKeyword(keyword);

        returnInfo.setKeyword(keyword);
        returnInfo.setProductGrpList(tempProdGrp);

        return returnInfo;
    }

    public List<ProductGrp> getProdGrpUsingKeyword(String keyword) {
        List<ProductGrp> returnInfo = new ArrayList<>();

        List<String> prodGrpIdList = new ArrayList<>();
        prodGrpIdList = List.copyOf(redisTemplate.opsForZSet().reverseRange(keyword, 0, 9));

        List<Product> tempProdList = new ArrayList<>();

        for(final String prodGrpId: prodGrpIdList) {
            ProductGrp tempProdGrp = new ProductGrp();

            Set prodAndPriceList = redisTemplate.opsForZSet().rangeWithScores(prodGrpId, 0, 9);
            Iterator<Object> prodPriceObj = prodAndPriceList.iterator();

            while (prodPriceObj.hasNext()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> prodPriceMap = objectMapper.convertValue(prodPriceObj.next(), Map.class);
                Product tempProduct = new Product();

                tempProduct.setProductId(prodPriceMap.get("value").toString());
                tempProduct.setPrice(Double.valueOf(prodPriceMap.get("score").toString()).intValue());
                tempProdList.add(tempProduct);
            }
            tempProdGrp.setProdGrpId(prodGrpId);
            tempProdGrp.setProductList(tempProdList);
            returnInfo.add(tempProdGrp);
        }

        return returnInfo;
    }
}
