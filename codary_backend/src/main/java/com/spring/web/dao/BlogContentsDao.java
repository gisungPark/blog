package com.spring.web.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.spring.web.dto.BlogContentsDto;
import com.spring.web.dto.BlogContentsLikeDto;
import com.spring.web.dto.BlogHashtagDto;
import com.spring.web.dto.HashtagDto;
import com.spring.web.dto.UserDto;

@Mapper
public interface BlogContentsDao {

    BlogContentsDto getContent(int blogContentsId) throws SQLException;

    int writeBlogContent(BlogContentsDto blogContent) throws SQLException;

    void writeHash(HashtagDto hash) throws SQLException;

    void writeBlogHash(BlogHashtagDto blogHash) throws SQLException;

    List<BlogContentsDto> listBlogContents(String blogId) throws SQLException;

    int modifyBlogContent(BlogContentsDto blogContent);

    void deleteOldHash(BlogContentsDto blogContent) throws SQLException;

    int deleteBlogContent(int blogContentsId);

    int deleteCommentOfPost(int blogContentsId);

    List<BlogContentsDto> getAllContents() throws SQLException;

    int writeLog(Map<String, Object> log) throws SQLException;

    void increaseContentsView(int blogContentsId) throws SQLException;

    BlogContentsLikeDto readContentLike(BlogContentsLikeDto like) throws SQLException;

    void contentLike(BlogContentsLikeDto like) throws SQLException;

    void increasePostLike(int blogContentsId) throws SQLException;

    void contentUnlike(BlogContentsLikeDto like) throws SQLException;

    void decreasePostLike(int blogContentsId) throws SQLException;

    void usergraphViewCount(int blogContentsId) throws SQLException;

    UserDto getUser(String blogId) throws Exception;

    List<HashtagDto> getHashtagOfPost(int blogContentsId) throws Exception;

    List<HashtagDto> selectHash(String keyword) throws Exception;

    HashtagDto findTagByValue(String value) throws Exception;

}
