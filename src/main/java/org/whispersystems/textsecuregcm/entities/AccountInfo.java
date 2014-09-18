package org.whispersystems.textsecuregcm.entities;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountInfo {
    //    @JsonProperty
    private Long id;
    @JsonProperty
    private String number;
    @JsonProperty
    private String nickname;
    @JsonProperty
    private Boolean gender;
    @JsonProperty
    private Integer age;
    @JsonProperty
    private String work;
    @JsonProperty
    private Long imageattachmentid;
    @JsonProperty
    private String sign;
    
    private List<String> friends;
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getNickname() {
        return this.nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public Boolean getGender() {
        return this.gender;
    }
    public void setGender(Boolean gender) {
        this.gender = gender;
    }
    public Integer getAge() {
        return this.age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public String getWork() {
        return this.work;
    }
    public void setWork(String work) {
        this.work = work;
    }
    public Long getImageattachmentid() {
        return this.imageattachmentid;
    }
    public void setImageattachmentid(Long imageattachmentid) {
        this.imageattachmentid = imageattachmentid;
    }
    public String getSign() {
        return this.sign;
    }
    public void setSign(String sign) {
        this.sign = sign;
    }
    
    public List<String> getFriends() {
		return friends;
	}
	public void setFriends(List<String> friends) {
		this.friends = friends;
	}
	public AccountInfo(Long id, String number, String nickname, Boolean gender,
            Integer age, String work, Long imageattachmentid, String sign) {
        this.id = id;
        this.number = number;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.work = work;
        this.imageattachmentid = imageattachmentid;
        this.sign = sign;
    }
    public AccountInfo(String number, String nickname, Boolean gender,
            Integer age, String work, Long imageattachmentid, String sign) {
        this.number = number;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.work = work;
        this.imageattachmentid = imageattachmentid;
        this.sign = sign;
    }
    public AccountInfo() {
    }
}
