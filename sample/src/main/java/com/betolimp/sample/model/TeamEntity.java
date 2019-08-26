package com.betolimp.sample.model;

import javax.persistence.*;

@Entity
@Table(
        name="teams",
        uniqueConstraints=
        @UniqueConstraint(columnNames = {"sport_id", "name"}, name = "testConstr")
)
public class TeamEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="teams_generator")
    @SequenceGenerator(name="teams_generator", sequenceName="teams_id_seq", allocationSize = 0)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private SportEntity sport;

    private String name;
    @Column(length = 111)
    private String testSport;


}