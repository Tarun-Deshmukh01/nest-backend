package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WishlistResponse {

    private List<WishlistItemResponse> items;
    private int totalItems;
}
