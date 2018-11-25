package com.net128.app.tinyurl;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table
@Entity
public class TinyUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
    @NotNull
	private String url;
	@NotNull
	private String hashedKey;
	private int timesAccessed;
	
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

	public int getTimesAccessed() {
		return timesAccessed;
	}

	public void setTimesAccessed(int timesAccessed) {
		this.timesAccessed = timesAccessed;
	}

	public String getHashedKey() {
		return hashedKey;
	}
	
	public void setHashedKey(String key) {
		this.hashedKey = key;
	}
}
