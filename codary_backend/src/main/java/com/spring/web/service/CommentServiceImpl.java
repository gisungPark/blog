package com.spring.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.web.dao.CommentDao;
import com.spring.web.dto.CommentCountDto;
import com.spring.web.dto.CommentDto;
import com.spring.web.dto.CommentToLikeDto;
import com.spring.web.dto.LogDto;
import com.spring.web.dto.UserDto;
import com.spring.web.dto.UserInfoDto;

@Service
public class CommentServiceImpl implements CommentService {

	@Autowired
	SqlSession sqlSession;

	@Override
	public List<CommentDto> listComment(String blogId, int blogContentsId) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<>();
		map.put("blogId", blogId);
		map.put("blogContentsId", blogContentsId);
		return sqlSession.getMapper(CommentDao.class).listComment(map);
	}

	@Override
	public UserInfoDto getUserInfo(UserInfoDto info) throws Exception {
		// TODO Auto-generated method stub
		if (info == null)
			return null;
		return sqlSession.getMapper(CommentDao.class).getUserInfo(info);
	}

	@Override
	public void writeComment(CommentDto comment) throws Exception {
		if (comment == null)
			return;
		sqlSession.getMapper(CommentDao.class).writeComment(comment);

	}

	@Override
	public void deleteComment(int commentNum) throws Exception {
		sqlSession.getMapper(CommentDao.class).deleteComment(commentNum);

	}

	@Override
	public void modifyComment(CommentDto comment) throws Exception {
		if (comment == null)
			return;
		sqlSession.getMapper(CommentDao.class).modifyComment(comment);
	}

	@Override
	public boolean getCommentLike(CommentToLikeDto ctl) throws Exception {
		CommentToLikeDto ctlDto = sqlSession.getMapper(CommentDao.class).getCommentLike(ctl);
		if (ctlDto == null)
			return false;
		else
			return true;
	}

	@Override
	@Transactional
	public void commentLike(CommentToLikeDto ctl) throws Exception {
		sqlSession.getMapper(CommentDao.class).upComment(ctl);
		sqlSession.getMapper(CommentDao.class).commentLike(ctl);
	}

	@Override
	@Transactional
	public void commentLikeCancle(CommentToLikeDto ctl) throws Exception {
		int count = sqlSession.getMapper(CommentDao.class).commentLikeCancle(ctl);
		if (count != 0) {
			sqlSession.getMapper(CommentDao.class).downComment(ctl);
		}
	}

//	@Cacheable(value = "commentCheck", key = "#user")
	@Override
	public List<CommentCountDto> commentCheck(UserDto user) throws Exception {

		// ?????????
		List<CommentCountDto> list = new ArrayList<>();

		// 1. uid??? ????????? log ??????????????? blog_content_id??? ????????? ?????? ??????
		List<LogDto> log = sqlSession.getMapper(CommentDao.class).getLog(user);
		HashMap<Integer, String> mylogTime = new HashMap<Integer, String>();
		Collections.sort(log,(o1,o2) -> o1.getDatetime().compareTo(o2.getDatetime()));

		// 2. 1?????? ??????????????? ????????? bolg_content_id ?????? ?????? ?????? datetime ??????
		for (LogDto lg : log) {
			mylogTime.put(lg.getBlogContentsId(), lg.getDatetime());
		}
		
		// 3. blogId ????????? comment ?????? ??? ??????
		List<CommentDto> commentList = sqlSession.getMapper(CommentDao.class).getComment(user);

		// 4. 2?????? 3?????? date time??? ???????????? map ???????????? ?????????
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		Collections.sort(commentList, (o1, o2) -> o1.getBlogContentsId() - o2.getBlogContentsId());

		// ??? ????????? ?????? ??????
		if (mylogTime.size() == 0) return null;

		for (CommentDto comments : commentList) {
			if(mylogTime.get(comments.getBlogContentsId()) == null) continue;
			LocalDateTime myLogTime = LocalDateTime.parse(mylogTime.get(comments.getBlogContentsId()),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			LocalDateTime commentLogTime = LocalDateTime.parse(comments.getCommentDatetime(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			if (!myLogTime.isBefore(commentLogTime)) continue;
			
			result.merge(comments.getBlogContentsId(), 1, Integer::sum);
//			if (result.get(comments.getBlogContentsId()) == null) result.put(comments.getBlogContentsId(), 1);
//			else result.put(comments.getBlogContentsId(), result.get(comments.getBlogContentsId()) + 1);
		}

		// 5. ?????? ????????? ?????????
		for (Integer blogCommentCount : result.keySet()) {
//			System.out.println(blogCommentCount + " : " + result.get(blogCommentCount));
			list.add(new CommentCountDto(blogCommentCount, result.get(blogCommentCount)));
		}

		if (list.size() == 0) return null;
		return list;
	}

}
