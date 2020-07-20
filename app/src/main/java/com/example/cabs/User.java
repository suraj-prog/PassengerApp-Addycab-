package com.example.cabs;

public class User {
    private String name;
    private String no;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNo() {
        return no;
    }

    public void setNo(  String no) {
        this.no = no;
    }
    public String toString(){
        return this.name+"\n"+no;
    }
}
