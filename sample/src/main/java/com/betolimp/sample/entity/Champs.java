package com.betolimp.sample.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Champs {
    @Id
    private Long id;
    private Integer sport_id;
    private String name;
    private String test;
}
