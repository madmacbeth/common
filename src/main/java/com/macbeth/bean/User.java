package com.macbeth.bean;

import com.google.common.collect.Lists;
import com.macbeth.base.Copyable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class User implements Copyable {
    private Integer aInteger;
    private Long aLong;
    private String aString;
    private User subUser;
    private List<String> strings = Lists.newArrayList();
}
