package com.teamright.brokurly.product.mapper;

import java.util.List;

import com.teamright.brokurly.model.CartVO;

public interface CartMapper {
    public List<CartVO> listCart(String customer_id); 
    public void delete(String msg); 
    public String sumMoney(String customer_id); 
    public String diff(String customer_id); 
    public int countCart(String customer_id, int product_id); 
    public void modify(CartVO vo);
}
