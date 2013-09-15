package com.natsuyuu.tabi;

public class UserData {
	private long id;
	private String key;
	private String value;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}

	/** reserved, will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return comment;
	}
	**/
}
