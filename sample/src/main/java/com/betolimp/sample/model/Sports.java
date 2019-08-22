package com.betolimp.sample.model;

import javax.persistence.*;

@Entity
public class Sports {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String camelCase;
    private String cmaelCaseOne;
    private String cmaelCaseTwo;
    private String cmaelCaseThree;
    @Column(length = 111)
    private String cmaelCaseFour;



}
