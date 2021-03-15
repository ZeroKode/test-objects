package com.github.zerokode.testobjects.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String name;
    private Status status;
    private int memberSince;
    private Set<Order> orders;

}
