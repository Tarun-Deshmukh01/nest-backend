package com.tarun.nest.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyname;

    private String sinceyear;

    @Column(columnDefinition = "TEXT")
    private String about;

    private String fb;

    private String insta;

    private String twitter;

    private String address;

    private String city;

    private String state;

    private String country;

    private String zipcode;

    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}