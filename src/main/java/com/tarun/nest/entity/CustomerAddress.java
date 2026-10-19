package com.tarun.nest.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_addresses")
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(length = 50)
    private String label;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(nullable = false, length = 100)
    private String country;

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User value) { customer = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getPincode() { return pincode; }
    public void setPincode(String value) { pincode = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
}
