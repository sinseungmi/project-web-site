package com.teamright.brokurly.customer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.teamright.brokurly.customer.mapper.MyPageOrderListMapper;
import com.teamright.brokurly.model.DetailOrderVO;
import com.teamright.brokurly.model.ProductVO;

@Service
public class MyPageOrderListServiceImpl implements MyPageOrderListService {

	@Autowired
	MyPageOrderListMapper orderListMapper;
	
	// Map을 이용해서 주문번호별 상품리스트 모두 정리
	@Override
	public List<List<ProductVO>> getOrderList(String customer_id) {
		List<DetailOrderVO> idAndCount = orderListMapper.getOrderIdAndCount(customer_id); // 주문번호 내역 추출
		List<ProductVO> products = orderListMapper.getListByOrder(customer_id); // 고객아이디를 받아서 모든 주문 상품 추출
		
		Map<Integer, List<ProductVO>> division = new HashMap<>();
		List<List<ProductVO>> orderList = new ArrayList<>();
		
		// 주문번호를 뽑아서 키 값으로 선정
		for (int i = 0; i < idAndCount.size(); i++) {
			division.put(idAndCount.get(i).getOrder_id(), new ArrayList<>());
		}
		// 주문번호별 상품 정보들 저장
		for (int i = 0; i < products.size(); i++) {
			for (Integer key : division.keySet()) {
				if (products.get(i).getOrder_id().equals(key)) {
					division.get(key).add(products.get(i));
				}
			}
		}
		// 분류한 리스트를 다시 리스트로 만듦
		for (Integer key : division.keySet()) {
			orderList.add(division.get(key));
		}
		
		return orderList;
	}
}
