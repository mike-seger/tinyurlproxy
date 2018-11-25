package com.net128.app.tinyurl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class Controller {
	private final static int maxHHashKeyLength=6;

	private static final Logger logger = LoggerFactory.getLogger(Controller.class);
	
	@Autowired
	private Repository tinyUrlRepository;

	@Autowired
	private ProxyService proxyService;
	
	@PostMapping(value = "api/tinyurl", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<TinyUrl> createTinyUrl(@RequestBody TinyUrl tinyUrlIn) {
		TinyUrl tinyUrl = tinyUrlRepository.findByUrl(tinyUrlIn.getUrl());
		if(tinyUrl==null) {
			tinyUrl = tinyUrlRepository.save(tinyUrlIn.getUrl(), maxHHashKeyLength);
		}
		return new ResponseEntity<TinyUrl>(tinyUrl, HttpStatus.CREATED);
	}

	@RequestMapping(value = "api/tinyurl/{hashedkey}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Void> deleteTinyUrl(@PathVariable("hashedkey") String hashedKey) {
		tinyUrlRepository.delete(tinyUrlRepository.findByHashedKey(hashedKey));
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "go/{hashedkey}", method = RequestMethod.GET)
	public void go(@PathVariable("hashedkey") String hashedKey, HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
		TinyUrl tinyUrl = tinyUrlRepository.findByHashedKey(hashedKey);
		if(tinyUrl != null) {
			proxyService.proxyRequest(request, response, tinyUrl.getUrl());
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
