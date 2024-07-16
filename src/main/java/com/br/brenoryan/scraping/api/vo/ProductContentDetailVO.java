package com.br.brenoryan.scraping.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductContentDetailVO {

    private String itemName;
    private ArrayList<String> images;
    private String videoLink;
    private String starRating;
    private String ratingCount;
    private String originalPrice;
    private String dealPrice;
    private String emiStartAmount;
    private ArrayList<String>offers;
    private String soldByDealer;
    private ArrayList<String> description;
    private HashMap<String,String> relianceDescriptionImageDto;
}
