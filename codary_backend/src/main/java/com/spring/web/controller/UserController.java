package com.spring.web.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.codecommit.model.UserInfo;
import com.spring.web.dto.LoginCallBackDto;
import com.spring.web.dto.UserDto;
import com.spring.web.dto.UserInfoDto;
import com.spring.web.service.BlogContentsService;
import com.spring.web.service.JwtServiceImpl;
import com.spring.web.service.UserService;
import com.spring.web.service.oauth.GoogleOauthService;
import com.spring.web.service.oauth.KakaoOauthService;
import com.spring.web.service.oauth.NaverOauthService;
import com.spring.web.service.oauth.SocialOauth;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = { "*" })
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private KakaoOauthService kakaoOauthService;
	@Autowired
	private NaverOauthService naverOauthService;
	@Autowired
	private GoogleOauthService googleOauthService;
	@Autowired
	private JwtServiceImpl jwtService;

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	
	/**
	 * ??????????????? access_token??? ?????? ?????? ?????? ?????? 
	 * 
	 * @param -
	 * @return List<BlogPostDto>
	 */
	@ApiOperation(value = "?????????????????? ?????? ????????? access_token???", notes ="@param : access_token  </br> @return : access_token, message, user")
	@PostMapping("/login/{socialLoginType}")
	public ResponseEntity<Map<String, Object>> access(@PathVariable("socialLoginType") String socialLoginType,
			@RequestBody String accessTocken) {
		System.out.println("#" + socialLoginType + "????????? ?????????!!");
		System.out.println("# accessTocken: " + accessTocken);
		SocialOauth socialOauth = getSocialOauth(socialLoginType);

		HashMap<String, Object> userInfo = socialOauth.getUserInfoFromOauth(accessTocken);
		logger.info("#Get userInfo: {}", userInfo);

		UserDto user = null;
		Map<String, Object> resultMap = new HashMap<>();
		LoginCallBackDto loginCallBackDto = new LoginCallBackDto();

		try {
			user = userService.findByProvider(userInfo);
			if (user == null) {
				logger.info("#?????? ??????????????????.");
				loginCallBackDto = userService.save(userInfo);
			} else {
				logger.info("#?????????????????????.");
				loginCallBackDto = getLoginCallBackByUid(user.getUid());
			}
			
			resultMap.put("message", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}

		System.out.println(loginCallBackDto.toString());
		String token = jwtService.create("uid", loginCallBackDto.getUid(), "access_token");
		logger.debug("#????????????: " + token);
		resultMap.put("access_token", token);
		resultMap.put("user", loginCallBackDto);

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	/**
	 * JWT ????????? ????????? ?????? ?????? ??????
	 * 
	 * @param -
	 * @return 
	 */
	@ApiOperation(value = "????????? ?????? JWT ?????? ?????? ??????, <br> ?????? ????????? ?????? ?????? ?????? ??? ??????", notes ="@param :  </br> @return : message, user")
	@GetMapping("/getUserInfo")
	public ResponseEntity<Map<String, Object>> getUserInfo(
			HttpServletRequest request ) {
		final String jwt = request.getHeader("access_token");
		String uid = null;
		uid = jwtService.getUserId(jwt);
		
		Map<String, Object> resultMap = new HashMap<>();
		LoginCallBackDto loginCallBackDto = null;
		try {
			loginCallBackDto = getLoginCallBackByUid(uid);
			resultMap.put("message", SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}
		
		logger.info("#Get userInfo: {}", loginCallBackDto);
//		String token = jwtService.create("uid", uid, "access_token");
//		logger.debug("#????????????: " + token);
//		resultMap.put("access_token", token);
		resultMap.put("user", loginCallBackDto);

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	
	@GetMapping("/getRefreshToken")
	public ResponseEntity<Map<String, Object>> getRefreshToken(	HttpServletRequest request ) {

		final String jwt = request.getHeader("access_token");
		Map<String, Object> resultMap = new HashMap<>();
		String uid = jwtService.getUserId(jwt);
		
		if(uid == null ) {
			resultMap.put("message", FAIL);
			return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
		}
		
		String token = jwtService.create("uid", uid, "access_token");
		logger.debug("#????????????: " + token);
		resultMap.put("access_token", token);
		resultMap.put("message", SUCCESS);
		
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	
	/**
	 * ????????? ?????? ?????????
	 * uid??? ????????? ?????? ?????? ??????
	 * 
	 * @param -
	 * @return 
	 */
	@GetMapping("/profileInfo/{uid}")
	public ResponseEntity<Map<String, Object>> profileInfo( 
			@PathVariable String uid ) {

		System.out.println("#"+ uid +" ????????? ?????? ??????");
		Map<String, Object> resultMap = new HashMap<>();
		UserInfoDto info;
		try {
			info = userService.findUserInfoById(uid);
			if(info == null ) {
				resultMap.put("message", "????????? ???????????? ?????????.");
			}else {
				resultMap.put("message", SUCCESS);
				resultMap.put("info", info);
				logger.info("#Get userInfo: {}", info);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			resultMap.put("message", "????????? ???????????? ?????????.");
		}
		
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}
	

	/**
	 * ?????? ?????? ??????
	 * 
	 * @param UserInfoDto
	 * @return 
	 */
	@ApiOperation(value = "?????? ?????? ??????", notes ="@param : UserInfoDto </br> @return : ")
	@PostMapping("/updateInfo")
	public ResponseEntity<Map<String, Object>> updateInfo(
			@RequestBody UserInfoDto info) {
		System.out.println("#???????????? ?????? ??????!!!");
		logger.info("#Get userInfo: {}", info);
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			userService.updateUserInfo(info);
			resultMap.put("message", "?????? ????????? ?????????????????????.");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("message", "?????? ????????? ?????????????????????.");
		}
		
		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

	@ApiOperation(value = "?????? ?????? ??????", notes ="@param : ????????? ?????? ?????????(uid)  </br> @return : ")
	@PostMapping("/delete/user")
	public ResponseEntity<Map<String, Object>> deleteUser(
//			HttpServletRequest request,
			@RequestParam("uid") String uid) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		System.out.println(uid+" ?????? ??????");
		
		try {
			// ?????? ?????? ?????? ????????????.
			UserDto user = userService.findById(uid);

			// User, ?????????, ????????? ???, ?????? ????????? ??????
			userService.delete(user.getUid(), user.getBlogId());
			resultMap.put("message", "success");

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultMap.put("message", e.getMessage());
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);
	}

	
	/**
	 * Frontend??? ????????? ???????????? ?????? ??????
	 * 
	 * @param uid
	 * @return LoginCallBackDto
	 */
	public LoginCallBackDto getLoginCallBackByUid(String uid) {
		LoginCallBackDto loginCallBackDto = null;
		try {
			UserDto user = userService.findById(uid);
			UserInfoDto info = userService.findUserInfoById(uid);
			
			loginCallBackDto = new LoginCallBackDto(uid, user.getMemoId(), user.getBlogId(),
										info.getNickname(), info.getProfile(), user.getProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return loginCallBackDto;
	}
	

	public SocialOauth getSocialOauth(String socialLoginType) {
		switch (socialLoginType) {
		case "kakao":
			return kakaoOauthService;
		case "naver":
			return naverOauthService;
		case "google":
			return googleOauthService;
		default:
			return null;
		}
	}

}
