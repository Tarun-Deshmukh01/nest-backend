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

    @Column(name = "recipient_name", length = 120)
    private String recipientName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(length = 255)
    private String landmark;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User value) { customer = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String value) { recipientName = value; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String value) { phoneNumber = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String value) { addressLine2 = value; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String value) { landmark = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getPincode() { return pincode; }
    public void setPincode(String value) { pincode = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { country = value; }
    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean value) { defaultAddress = value; }
}
