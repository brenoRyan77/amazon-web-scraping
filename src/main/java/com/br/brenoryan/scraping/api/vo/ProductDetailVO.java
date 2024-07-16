package com.br.brenoryan.scraping.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailVO {

    private String title;
    private String url;
    private String price;
    private String image;
    private ArrayList<String> description;
}
