package com.betolimp.sample.model;

import javax.persistence.*;

@Entity
public class REW {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="REW_generator")
    @SequenceGenerator(name="REW_generator", sequenceName="REW_id_seq", allocationSize = 0)
    int id;

    private String sdkgsdg;

}
