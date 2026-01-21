package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.RestaurantService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminRestaurantControllerTest {

	@Autowired
	   private MockMvc mockMvc;

	   @Autowired
	   private RestaurantService restaurantService;

	   @Test
	   public void 未ログインの場合は管理者用の店舗一覧ページからログインページにリダイレクトする() throws Exception {
	       mockMvc.perform(get("/admin/restaurants"))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("http://localhost/login"));
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   public void 一般ユーザーとしてログインしている場合は管理者用の店舗一覧ページが表示されずに403エラーが発生する() throws Exception {
	       mockMvc.perform(get("/admin/restaurants"))
	              .andExpect(status().isForbidden());
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   public void 管理者としてログイン済みしている管理者用の店舗一覧ページが正しく表示される() throws Exception {
	       mockMvc.perform(get("/admin/restaurants"))
	              .andExpect(status().isOk())
	              .andExpect(view().name("admin/restaurants/index"));
	   }

	   @Test
	   public void 未ログインの場合は管理者用の店舗詳細ページからログインページにリダイレクトする() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1"))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("http://localhost/login"));
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   public void 一般ユーザーとしてログインしている場合は管理者用の店舗詳細ページが表示されずに403エラーが発生する() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1"))
	              .andExpect(status().isForbidden());
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   public void 管理者としてログインしている場合は管理者用の店舗詳細ページが正しく表示される() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1"))
	              .andExpect(status().isOk())
	              .andExpect(view().name("admin/restaurants/show"));
	   }

	   @Test
	   public void 未ログインの場合は管理者用の店舗登録ページからログインページにリダイレクトする() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/register"))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("http://localhost/login"));
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   public void 一般ユーザーとしてログインしている場合は管理者用の店舗登録ページが表示されずに403エラーが発生する() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/register"))
	              .andExpect(status().isForbidden());
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   public void 管理者としてログインしている場合は管理者用の店舗登録ページが正しく表示される() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/register"))
	              .andExpect(status().isOk())
	              .andExpect(view().name("admin/restaurants/register"));
	   }

	   @Test
	   @Transactional
	   public void 未ログインの場合は店舗を登録せずにログインページにリダイレクトする() throws Exception {
	       // テスト前のレコード数を取得する
	       long countBefore = restaurantService.countRestaurants();

	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00")
	               .param("regularHolidayIds", "1")
	               .param("regularHolidayIds", "2"))
	           .andExpect(status().is3xxRedirection())
	           .andExpect(redirectedUrl("http://localhost/login"));

	       // テスト後のレコード数を取得する
	       long countAfter = restaurantService.countRestaurants();

	       // レコード数が変わっていないことを検証する
	       assertThat(countAfter).isEqualTo(countBefore);
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   @Transactional
	   public void 一般ユーザーとしてログインしている場合は店舗を登録せずに403エラーが発生する() throws Exception {
	       // テスト前のレコード数を取得する
	       long countBefore = restaurantService.countRestaurants();

	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00")
	               .param("regularHolidayIds", "1")
	               .param("regularHolidayIds", "2"))
	           .andExpect(status().isForbidden());

	       // テスト後のレコード数を取得する
	       long countAfter = restaurantService.countRestaurants();

	       // レコード数が変わっていないことを検証する
	       assertThat(countAfter).isEqualTo(countBefore);
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   @Transactional
	   public void 管理者としてログインしている場合は店舗登録後に店舗一覧ページにリダイレクトする() throws Exception {
	       // テスト前のレコード数を取得する
	       long countBefore = restaurantService.countRestaurants();

	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00"))
	           .andExpect(status().is3xxRedirection())
	           .andExpect(redirectedUrl("/admin/restaurants"));

	      
	       long countAfter = restaurantService.countRestaurants();

	       assertThat(countAfter).isEqualTo(countBefore + 1);

	       Restaurant restaurant = restaurantService.findFirstRestaurantByOrderByIdDesc();
	       String[] part = restaurant.getBusinessHours().split("~");
			LocalTime openingTime = LocalTime.parse(part[0]);
			LocalTime closingTime = LocalTime.parse(part[1]);
			
	       assertThat(restaurant.getName()).isEqualTo("テスト店舗名");
	       assertThat(restaurant.getDescription()).isEqualTo("テスト説明");
	       assertThat(restaurant.getLowestPrice()).isEqualTo(3000);
	       assertThat(restaurant.getHighestPrice()).isEqualTo(8000);
	       assertThat(restaurant.getPostalCode()).isEqualTo("0000000");
	       assertThat(restaurant.getAddress()).isEqualTo("テスト住所");
	       assertThat(restaurant.getPhoneNumber()).isEqualTo("0123456789");
	       assertThat(openingTime).isEqualTo("10:00");
	       assertThat(closingTime).isEqualTo("22:00");
	   }

	   @Test
	   public void 未ログインの場合は管理者用の店舗編集ページからログインページにリダイレクトする() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1/edit"))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("http://localhost/login"));
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   public void 一般ユーザーとしてログインしている場合は管理者用の店舗編集ページが表示されずに403エラーが発生する() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1/edit"))
	              .andExpect(status().isForbidden());
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   public void 管理者としてログインしている場合は管理者用の店舗編集ページが正しく表示される() throws Exception {
	       mockMvc.perform(get("/admin/restaurants/1/edit"))
	              .andExpect(status().isOk())
	              .andExpect(view().name("admin/restaurants/edit"));
	   }

	   @Test
	   @Transactional
	   public void 未ログインの場合は店舗を更新せずにログインページにリダイレクトする() throws Exception {
	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/1/update").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00")
	               .param("regularHolidayIds", "1")
	               .param("regularHolidayIds", "2"))
	           .andExpect(status().is3xxRedirection())
	           .andExpect(redirectedUrl("http://localhost/login"));

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isPresent();
	       Restaurant restaurant = optionalRestaurant.get();
	       
	       String[] part = restaurant.getBusinessHours().split("~");
			LocalTime openingTime = LocalTime.parse(part[0]);
			LocalTime closingTime = LocalTime.parse(part[1]);
			assertThat(restaurant.getName()).isEqualTo("定番TEBASAKI");
		       assertThat(restaurant.getDescription()).isEqualTo("これは遅くまでやっている手羽先屋さんです");
		       assertThat(restaurant.getLowestPrice()).isEqualTo(3000);
		       assertThat(restaurant.getHighestPrice()).isEqualTo(6000);
		       assertThat(restaurant.getPostalCode()).isEqualTo("1234567");
		       assertThat(restaurant.getAddress()).isEqualTo("愛知県名古屋市桜川X-XX-XX");
		       assertThat(restaurant.getPhoneNumber()).isEqualTo("0120123456");
		       assertThat(openingTime).isEqualTo("09:00");
		       assertThat(closingTime).isEqualTo("23:00");
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   @Transactional
	   public void 一般ユーザーとしてログインしている場合は店舗を更新せずに403エラーが発生する() throws Exception {
	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/1/update").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00")
	               .param("regularHolidayIds", "1")
	               .param("regularHolidayIds", "2"))
	           .andExpect(status().isForbidden());

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isPresent();
	       Restaurant restaurant = optionalRestaurant.get();
	       String[] part = restaurant.getBusinessHours().split("~");
			LocalTime openingTime = LocalTime.parse(part[0]);
			LocalTime closingTime = LocalTime.parse(part[1]);
			
	       assertThat(restaurant.getName()).isEqualTo("定番TEBASAKI");
	       assertThat(restaurant.getDescription()).isEqualTo("これは遅くまでやっている手羽先屋さんです");
	       assertThat(restaurant.getLowestPrice()).isEqualTo(3000);
	       assertThat(restaurant.getHighestPrice()).isEqualTo(6000);
	       assertThat(restaurant.getPostalCode()).isEqualTo("1234567");
	       assertThat(restaurant.getAddress()).isEqualTo("愛知県名古屋市桜川X-XX-XX");
	       assertThat(restaurant.getPhoneNumber()).isEqualTo("0120123456");
	       assertThat(openingTime).isEqualTo("09:00");
	       assertThat(closingTime).isEqualTo("23:00");
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   @Transactional
	   public void 管理者としてログインしている場合は店舗更新後に店舗一覧ページにリダイレクトする() throws Exception {
	       // テスト用の画像ファイルデータを準備する
	       Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
	       String fileName = filePath.getFileName().toString();
	       String fileType = Files.probeContentType(filePath);
	       byte[] fileBytes = Files.readAllBytes(filePath);

	       MockMultipartFile imageFile = new MockMultipartFile(
	           "imageFile",  // フォームのname属性の値
	           fileName,     // ファイル名
	           fileType,     // ファイルの形式
	           fileBytes     // ファイルのバイト配列
	       );

	       mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/1/update").file(imageFile)
	               .with(csrf())
	               .param("name", "テスト店舗名")
	               .param("description", "テスト説明")
	               .param("lowestPrice", "3000")
	               .param("highestPrice", "8000")
	               .param("postalCode", "0000000")
	               .param("address", "テスト住所")
	               .param("phoneNumber", "0123456789")
	               .param("openingTime", "10:00")
	               .param("closingTime", "22:00")
	               .param("categoryIds", "1")
	               .param("categoryIds", "2")
	               .param("regularHolidayIds", "1")
	               .param("regularHolidayIds", "2"))
	           .andExpect(status().is3xxRedirection())
	           .andExpect(redirectedUrl("/admin/restaurants"));

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isPresent();
	       Restaurant restaurant = optionalRestaurant.get();
	       
	       String[] part = restaurant.getBusinessHours().split("~");
			LocalTime openingTime = LocalTime.parse(part[0]);
			LocalTime closingTime = LocalTime.parse(part[1]);
	       assertThat(restaurant.getName()).isEqualTo("テスト店舗名");
	       assertThat(restaurant.getDescription()).isEqualTo("テスト説明");
	       assertThat(restaurant.getLowestPrice()).isEqualTo(3000);
	       assertThat(restaurant.getHighestPrice()).isEqualTo(8000);
	       assertThat(restaurant.getPostalCode()).isEqualTo("0000000");
	       assertThat(restaurant.getAddress()).isEqualTo("テスト住所");
	       assertThat(restaurant.getPhoneNumber()).isEqualTo("0123456789");
	       assertThat(openingTime).isEqualTo("10:00");
	       assertThat(closingTime).isEqualTo("22:00");
	   }

	   @Test
	   @Transactional
	   public void 未ログインの場合は店舗を削除せずにログインページにリダイレクトする() throws Exception {
	       mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("http://localhost/login"));

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isPresent();
	   }

	   @Test
	   @WithUserDetails("yamadaA@example.com")
	   @Transactional
	   public void 一般ユーザーとしてログインしている場合は店舗を削除せずに403エラーが発生する() throws Exception {
	       mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
	              .andExpect(status().isForbidden());

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isPresent();
	   }

	   @Test
	   @WithUserDetails("satou@example.com")
	   @Transactional
	   public void 管理者としてログイんしている場合は店舗削除後に店舗一覧ページにリダイレクトする() throws Exception {
	       mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
	              .andExpect(status().is3xxRedirection())
	              .andExpect(redirectedUrl("/admin/restaurants"));

	       Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
	       assertThat(optionalRestaurant).isEmpty();
	   }
}
