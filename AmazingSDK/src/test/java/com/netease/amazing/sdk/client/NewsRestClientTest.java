package com.netease.amazing.sdk.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.netease.amazing.sdk.dto.NewsCommentsDTO;
import com.netease.amazing.sdk.dto.NewsDTO;
import com.netease.amazing.sdk.dto.NewsDTO.TweetType;

public class NewsRestClientTest extends BaseTest{
	@Test
	public void getLatestNewsTest() throws ClientProtocolException, IOException, URISyntaxException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		List<NewsDTO> news = newsClient.getLatestNews(15);
		for(NewsDTO n : news){
			System.out.println(n.getNewsId());
		}
	}
	
	@Test
	public void getRangeTest() throws ClientProtocolException, IOException, URISyntaxException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		List<NewsDTO> news = newsClient.getNewsByUpRefresh(7, 5);
		/*for(NewsDTO n : news){
			System.out.println(n.getNewsContent());
		}*/
		
		news = newsClient.getNews(2);
		for(NewsDTO n : news){
			System.out.println(n.getNewsContent());
		}
	}
	
	@Test 
	public void getCommentsTest() throws ClientProtocolException, IOException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		 List<NewsCommentsDTO>  comments = newsClient.getNewsCommentToNewsIndexByNewsId(1, 20);
		 for(NewsCommentsDTO c : comments){
			 System.out.println(c.getNewsCommentType());
		 }
	}
	
	@Test
	public void likeCommentTest() throws ClientProtocolException, IOException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		System.out.println(newsClient.setLikeNews(1));;
	}
	

	@Test
	public void includeCommentTest() throws ClientProtocolException, IOException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		System.out.println(newsClient.includeNews(1));;
	}
	
	@Test
	public void addNewsTest() throws ClientProtocolException, IOException{
		NewsRestClient newsClient = new NewsRestClient(this.BASE_URL,this.USER_NAME,this.PASSWORD);
		NewsDTO newsDTO = new NewsDTO();
		newsDTO.setNewsContent("���ڳ�����");
		newsDTO.setNewsType(TweetType.TEXT);
		assertTrue(newsClient.addNews(newsDTO));
	}
}
