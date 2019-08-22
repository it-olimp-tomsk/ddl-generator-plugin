package com.betolimp.sample.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="sportsss")
public class SportEntity {

    @Id
    private Long id;

    @Transient
    private boolean isNew;

    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "name ASC")
    private List<TeamEntity> teams;


}
