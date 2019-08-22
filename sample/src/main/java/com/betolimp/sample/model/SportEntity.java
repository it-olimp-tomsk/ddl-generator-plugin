package com.betolimp.sample.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="sports")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SportEntity {

    @Id
    private Long id;

    @Transient
    private boolean isNew;

    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "name ASC")
    private List<TeamEntity> teams;


}
