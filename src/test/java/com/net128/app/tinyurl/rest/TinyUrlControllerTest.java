package com.net128.app.tinyurl.rest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.net128.app.tinyurl.Controller;
import com.net128.app.tinyurl.TinyUrl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.net128.app.tinyurl.Application;
import com.net128.app.tinyurl.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=Application.class)
@WebAppConfiguration
@AutoConfigureMockMvc
public class TinyUrlControllerTest {
	
	@Autowired
	Controller tinyUrlController;
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	Repository tinyUrlRepository;
	
	@Test
	public void testCreateTinyUrl() throws Exception {
    	TinyUrl tinyUrl = new TinyUrl("http://testing.com/");
    	tinyUrl.setId(999L);
    	tinyUrl.setTimesAccessed(0);
    	
    	given(this.tinyUrlRepository.save(any(TinyUrl.class))).willReturn(tinyUrl);
    	ObjectMapper mapper = new ObjectMapper();
    	String tinyUrlAsJSON = mapper.writeValueAsString(tinyUrl);
    	System.out.println(tinyUrlAsJSON);
		//WHEN making an http request to create a tinyUrl
    	MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/api/tinyUrl")
    			.contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(tinyUrlAsJSON)
    			.accept(MediaType.APPLICATION_JSON_VALUE);
    	
    	ResultActions resultActions = mockMvc.perform(builder);
    	
    	//THEN expect the newly created tinyUrl to be returned
    	resultActions
    		.andExpect(status().isCreated())
	    	.andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id").value(999))
            .andExpect(jsonPath("$.originalUrl").value(tinyUrl.getOriginalUrl()))
            .andExpect(jsonPath("$.hashedKey").value("qh"))
            .andExpect(jsonPath("$.timesAccessed").value(tinyUrl.getTimesAccessed()));

	}
	
	@Test
	public void testGetTinyUrlByHashedKey() throws Exception {
		TinyUrl tinyUrl = new TinyUrl("testing.com");
		tinyUrl.setId(7628L);
		tinyUrl.setHashedKey("b9c");
		tinyUrl.setTimesAccessed(3);
		given(this.tinyUrlRepository.findOne(7628L)).willReturn(tinyUrl);
		
		//WHEN making an http request to create a tinyUrl
		this.mockMvc.perform(get("/api/tinyUrl/b9c")
							.accept(MediaType.APPLICATION_JSON_VALUE)
							.contentType(MediaType.APPLICATION_JSON_VALUE))
							//THEN expect the newly created tinyUrl to be returned
							.andExpect(status().isOk())
							.andExpect(content().contentType("application/json;charset=UTF-8"))
				            .andExpect(jsonPath("$.id").value(7628))
				            .andExpect(jsonPath("$.originalUrl").value("testing.com"))
				            .andExpect(jsonPath("$.hashedKey").value("b9c"))
				            .andExpect(jsonPath("$.timesAccessed").value("3"));
	}
	
	@Test
	public void testGetTinyUrlByHashedKeyNotFound() throws Exception {
		TinyUrl tinyUrl = new TinyUrl("testing.com");
		tinyUrl.setId(7628L);
		tinyUrl.setHashedKey("b9c");
		tinyUrl.setTimesAccessed(3);
		given(this.tinyUrlRepository.findOne(7628L)).willReturn(null);
		
		//WHEN making an http request to create a tinyUrl
		this.mockMvc.perform(get("/api/tinyUrl/b9c")
							.accept(MediaType.APPLICATION_JSON_VALUE)
							.contentType(MediaType.APPLICATION_JSON_VALUE))
							//THEN expect the newly created tinyUrl to be returned
							.andExpect(status().isNoContent());
	}
	
	@Test
	public void testDeleteTinyUrl() throws Exception {
		
		//WHEN making an http request to create a tinyUrl
		this.mockMvc.perform(delete("/api/tinyUrl/b9c"))
							//THEN expect the tinyUrl to be deleted
							.andExpect(status().isNoContent());	
		
		verify(tinyUrlRepository, times(1)).delete(TinyUrl.getIdFromHashedKey("b9c"));
	}

	@Test
	public void testRedirectUserToOriginalUrlAndIncrementsTimesAccessed() throws Exception {
		TinyUrl expected = new TinyUrl("testing.com");
		expected.setId(7628L);
		expected.setTimesAccessed(4);
		
		TinyUrl tinyUrl = new TinyUrl("testing.com");
		tinyUrl.setId(7628L);
		tinyUrl.setHashedKey("b9c");
		tinyUrl.setTimesAccessed(3);
		given(this.tinyUrlRepository.findOne(7628L)).willReturn(tinyUrl);
		
		//WHEN making an http request to navigate through a short url
		this.mockMvc.perform(get("/go/b9c"))
							//THEN the user should be redirected successfully
							.andExpect(status().isTemporaryRedirect())
							//AND the cache header should be cleared so that we can still increment the times accessed
							.andExpect(header().string("Cache-Control", equalTo("no-cache, no-store, must-revalidate")))
							.andExpect(header().string("Pragma", equalTo("no-cache")))
							.andExpect(header().string("Expires", equalTo("0")))
							.andExpect(header().string("Location", equalTo(tinyUrl.getOriginalUrl())));
		
		verify(tinyUrlRepository, times(1)).save(argThat(new TinyUrlMatcher(expected)));
	}
	

	@Test
	public void testNotFoundWhenRedirectingUserToOriginalUrl() throws Exception {
		
		given(this.tinyUrlRepository.findOne(7628L)).willReturn(null);
		
		//WHEN making an http request to navigate through a short url
		this.mockMvc.perform(get("/go/b9c"))
							//THEN the result should have status no content
							.andExpect(status().isNoContent());
	}
	
	public class TinyUrlMatcher extends ArgumentMatcher<TinyUrl> {
		 
	    private TinyUrl expectedTinyUrl;
	    
	    public TinyUrlMatcher(TinyUrl tinyUrl) {
	    	this.expectedTinyUrl = tinyUrl;
	    }

		@Override
		public boolean matches(Object actual) {
			TinyUrl actualTinyUrl = (TinyUrl) actual;
			return expectedTinyUrl.getOriginalUrl().equals(actualTinyUrl.getOriginalUrl())
					&& expectedTinyUrl.getId().equals(actualTinyUrl.getId())
					&& expectedTinyUrl.getOriginalUrl().equals(actualTinyUrl.getOriginalUrl());
		}
	}

}
