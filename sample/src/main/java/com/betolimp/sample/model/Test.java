package com.betolimp.sample.model;

import javax.persistence.*;

@Entity
public class Test {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="teams_generator")
    @SequenceGenerator(name="teams_generator", sequenceName="teams_id_seq", allocationSize = 0)
    private int id;

    private String sdfgadfgdfg;

}
