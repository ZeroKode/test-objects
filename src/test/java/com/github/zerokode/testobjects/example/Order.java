package com.github.zerokode.testobjects.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String id;
    private Instant date;
    private Set<Product> products;
    private Double totalPaid;
}
