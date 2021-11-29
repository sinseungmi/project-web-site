package com.teamright.brokurly.product.controller;
  

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.teamright.brokurly.model.CartVO;
import com.teamright.brokurly.model.CustomerVO;
import com.teamright.brokurly.model.MainCategoryVO;
import com.teamright.brokurly.model.ProductDetailVO;
import com.teamright.brokurly.model.ProductVO;
import com.teamright.brokurly.product.mapper.ProductDetailMapper;
import com.teamright.brokurly.product.service.ProductDetailServiceImpl;

import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/products")
public class ProductDetailController {
	
	@Autowired
	ProductDetailMapper productDetailMapper;
	
	@Autowired
	ProductDetailServiceImpl productDetailServiceImpl;
	
	@RequestMapping(value = "/product_code", method=RequestMethod.GET, produces="application/text; charset=utf8")
    @ResponseBody
    public String productCode(HttpServletRequest request) {
        String product_id = request.getParameter("product_id");
        
        return product_id; 
	}  
	
	//상세페이지에 상품 정보를 띄우기 위한 컨트롤러
    @GetMapping(value = "/product_detail_page")
	public String productDetailPage(@RequestParam("product_id") int product_id, Model model, HttpSession session) {
    	String customer_id = (String)session.getAttribute("customer_id");
    	
    	model.addAttribute("customer_id", customer_id);
    	model.addAttribute("product_id", product_id);
		
		//할인율, 할인된 가격 들고 가기
		List<ProductVO> discount = productDetailMapper.select_product_discount(product_id);
		//금액 콤마 적용
		DecimalFormat formatter = new DecimalFormat("###,###");
	
	    model.addAttribute("discont", discount.get(0).getEvent_discount());
	    model.addAttribute("discont_price", formatter.format(discount.get(0).getProduct_price()));
	    
		//ProductVO객체 모델에 어트리뷰트
		List<ProductVO> products =  productDetailMapper.select_product(product_id);
		ProductVO product_model = products.get(0);
		model.addAttribute("proM", product_model);
		model.addAttribute("price", formatter.format(product_model.getProduct_price()));
		
		if (customer_id != null) {
			//적립금 구하고 적립금, 회원 등급, 적립 퍼센트 어트리 뷰트
			CustomerVO cvo = productDetailServiceImpl.get_customer_grade_percent(customer_id);
			int accumulate = 
					(int) Math.round(Math.abs(discount.get(0).getProduct_price() * 
					((100 - cvo.getEarned_percentage()) / 100.0) - discount.get(0).getProduct_price()));
			model.addAttribute("percentage", cvo.getEarned_percentage());
			model.addAttribute("grade", cvo.getCustomer_grade());
			model.addAttribute("accumulate", formatter.format(accumulate));
			String count = Integer.toString(productDetailMapper.get_cart_count_share(customer_id));
			model.addAttribute("cart_count", count);
		    		
		}else {
			model.addAttribute("nonono", discount.get(0).getProduct_price());
		}
		
		//상품 설명을 리스트에 넣기
		ArrayList<String> contente = productDetailServiceImpl.save_contente(product_id);
		//메인 카테고리 id 가져오기
		MainCategoryVO main = productDetailMapper.select_main_category(product_id);
		
		//리스트에 상품 설명이 들어있는지 확인. 있으면 상세 페이지로,없으면 상품 준비 중 페이지로 넘어간다
		if (contente.isEmpty()) {
			return "/products/preparing_product_page";
		}else {
			model.addAttribute("content", contente);
			model.addAttribute("main_id", main.getMaincate_id());
			if (contente.indexOf("") != -1) {
				model.addAttribute("empty_index", contente.indexOf(""));			
			}else {
				model.addAttribute("empty_index", contente.size());
			}
			return "/products/product_detail_page";
		}
		
	}
    

    //상품 id, 고객 id, 상품수량을 db cart_table에 저장
    @ResponseBody
    @RequestMapping(value = "/product_detail_cart", method=RequestMethod.GET, produces="application/text; charset=utf8")
    public String get_product_info(HttpServletRequest request, HttpSession session) {
    	int quantity = Integer.parseInt(request.getParameter("quantity"));
    	int product_id = Integer.parseInt(request.getParameter("product_id"));
    	String customer_id = (String)session.getAttribute("customer_id");
    	
    	//상품 cart_table에 저장
    	int cart_count = productDetailServiceImpl.get_count(customer_id, product_id);
    	if (cart_count == 0) {
    		productDetailServiceImpl.save_product_info_cart(quantity, product_id, customer_id);    		
    	}else {
    		productDetailServiceImpl.set_cart_count(quantity, customer_id, product_id);
    	}
    	
    	return customer_id;
    }
  
}
