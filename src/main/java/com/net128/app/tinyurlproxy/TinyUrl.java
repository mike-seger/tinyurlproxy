package com.net128.app.tinyurlproxy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table
@Entity
public class TinyUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(unique=true, length=1024)
	private String url;
	@Column(unique=true)
	private String hashedKey;
	
	public TinyUrl() {
		super();
	}
	
	public TinyUrl(String url) {
		this.url = url;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHashedKey() {
		return hashedKey;
	}
	
	public void setHashedKey(String key) {
		this.hashedKey = key;
	}
}
