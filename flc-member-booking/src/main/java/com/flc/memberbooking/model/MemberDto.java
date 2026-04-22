package com.flc.memberbooking.model;

public class MemberDto {
    private Integer id;
    private String name;

    public MemberDto() {}

    public MemberDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
