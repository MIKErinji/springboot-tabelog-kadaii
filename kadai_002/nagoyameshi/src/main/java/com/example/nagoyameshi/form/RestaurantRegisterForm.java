package com.example.nagoyameshi.form;

import java.time.LocalTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RestaurantRegisterForm {
	
	    @NotBlank(message = "店舗名を入力してください。")
	    private String name;
	    
	    private MultipartFile imageFile;    
	        
	    @NotBlank(message = "説明を入力してください。")
	    private String description;    
	    
	    @NotNull(message = "最低価格を入力してください。")
	    private Integer lowestPrice;
	    
	    @NotNull(message = "最高価格を入力してください。")
	    private Integer highestPrice;
	    
	    @NotNull(message = "開店時間を入力してください。")
	    private LocalTime openingTime;     
	    
	    @NotNull(message = "閉店時間を入力してください。")
	    private LocalTime closingTime;     
	        
	    @NotBlank(message = "郵便番号を入力してください。")
	    @Pattern(regexp = "^[0-9]{7}$", message = "郵便番号は7桁の半角数字で入力してください。")
	    private String postalCode;
	        
	    @NotBlank(message = "住所を入力してください。")
	    private String address;
	        
	    @NotBlank(message = "電話番号を入力してください。")
	    @Pattern(regexp = "^[0-9]{10,11}$", message = "電話番号は10桁または11桁の半角数字で入力してください。")
	    private String phoneNumber;
	    
	    private List<Integer> categoryIds;
	    
	    private List<Integer> regularHolidayIds;
	    
}
