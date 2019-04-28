package com.network.udpchat.common.domain;

public class Friends {
    private Integer id;

    private String posFriend;

    private String negFriend;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPosFriend() {
        return posFriend;
    }

    public void setPosFriend(String posFriend) {
        this.posFriend = posFriend == null ? null : posFriend.trim();
    }

    public String getNegFriend() {
        return negFriend;
    }

    public void setNegFriend(String negFriend) {
        this.negFriend = negFriend == null ? null : negFriend.trim();
    }
}