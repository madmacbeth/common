package com.macbeth.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserVo {

    private Integer aInteger;
    private Long aLong;
    private String aString;
    private User subUser;
}
