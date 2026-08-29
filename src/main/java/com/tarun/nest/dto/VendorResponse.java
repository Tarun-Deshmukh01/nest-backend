package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {

    private Long id;

    private CompanyDetail companyDetail;

    private SocialLinks socialLinks;

    private BusinessAddress businessAddress;
    
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyDetail {

        private String companyname;

        private String sinceyear;

        private String about;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLinks {

        private String fb;

        private String insta;

        private String twitter;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessAddress {

        private String address;

        private String city;

        private String state;

        private String country;

        private String zipcode;
    }
}