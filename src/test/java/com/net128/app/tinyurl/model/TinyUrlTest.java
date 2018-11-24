package com.net128.app.tinyurl.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.net128.app.tinyurl.TinyUrl;
import org.junit.Test;

public class TinyUrlTest {
	
	@Test
	public void testGetIdFromHashedKey() {
		verifyHashedKeyMapsToId("b9c", 7628L);
		verifyHashedKeyMapsToId("A12ac", 397023698L);
		verifyHashedKeyMapsToId("fI", 344L);
	}
	
	@Test
	public void hashedKeyIsSetAutomatically() {
		//Given a TinyUrl with an ID
		TinyUrl tinyUrl = new TinyUrl("http://test.com");
		tinyUrl.setId(344L);
		//Then the hashedKey will be set automatically
		assertThat(tinyUrl.getHashedKey()).isEqualTo("fI");
	}
	
	private void verifyHashedKeyMapsToId(String expectedHashedKey, Long expectedId) {
		
		//When looking up the ID from the hashed key
		Long actualId = TinyUrl.getIdFromHashedKey(expectedHashedKey);
		
		//Then the ID should match the expected
		assertThat(actualId).isEqualTo(expectedId);
		
		//When looking up the hashedKey from the id
		String actualHashedKey = TinyUrl.getHashedKeyFromId(expectedId);
		
		//Then the hashedKey should match the expectedHashedKey
		assertThat(actualHashedKey).isEqualTo(expectedHashedKey);
		
		
	}

}
