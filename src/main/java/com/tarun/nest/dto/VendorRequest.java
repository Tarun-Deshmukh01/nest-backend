package com.tarun.nest.dto;

import lombok.Data;

@Data
public class VendorRequest {

    private CompanyDetail companyDetail;

    private SocialLinks socialLinks;
    
    private BusinessAddress businessAddress;

    @Data
    public static class CompanyDetail {
        private String companyname;
        private String sinceyear;
        private String about;
    }

    @Data
    public static class SocialLinks {
        private String fb;
        private String insta;
        private String twitter;
    }
    
    @Data
    public static class BusinessAddress {
    	private String address;
		private String city;
		private String state;
		private String country;
		private String zipcode;
    }
}