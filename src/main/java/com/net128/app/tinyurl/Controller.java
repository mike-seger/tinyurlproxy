package com.net128.app.tinyurl;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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

import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class Controller {
	
	@Autowired
	private Repository tinyUrlRepository;
	
	@PostMapping(value = "api/tinyurl", produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<TinyUrl> createTinyUrl(@RequestBody @Valid TinyUrl tinyUrlIn) {
		TinyUrl tinyUrl = new TinyUrl(tinyUrlIn.getOriginalUrl());
		tinyUrl = tinyUrlRepository.save(tinyUrl);
		return new ResponseEntity<TinyUrl>(tinyUrl, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "api/tinyurl/{hashedkey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<TinyUrl> createTinyUrl(@PathVariable("hashedkey") String hashedKey) {
		TinyUrl tinyUrl = this.tinyUrlRepository.findOne(TinyUrl.getIdFromHashedKey(hashedKey));
		if(tinyUrl != null) {
			return new ResponseEntity<TinyUrl>(tinyUrl, HttpStatus.OK);
		} else {
			return new ResponseEntity<TinyUrl>(HttpStatus.NO_CONTENT);
		}
	}
	
	@RequestMapping(value = "api/tinyurl/{hashedkey}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Void> deleteTinyUrl(@PathVariable("hashedkey") String hashedKey) {
		this.tinyUrlRepository.delete(TinyUrl.getIdFromHashedKey(hashedKey));
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value = "go/{hashedkey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void redirectUserToOriginalUrl(@PathVariable("hashedkey") String hashedKey, HttpServletResponse response) {
		TinyUrl tinyUrl = this.tinyUrlRepository.findOne(TinyUrl.getIdFromHashedKey(hashedKey));
		if(tinyUrl != null) {
			tinyUrl.setTimesAccessed(tinyUrl.getTimesAccessed()+1);
			tinyUrlRepository.save(tinyUrl);
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");
			response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
			response.setHeader("Location", tinyUrl.getOriginalUrl());
		} else {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
}
