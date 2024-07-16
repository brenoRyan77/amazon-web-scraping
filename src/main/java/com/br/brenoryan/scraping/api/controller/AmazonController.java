package com.br.brenoryan.scraping.api.controller;

import com.br.brenoryan.scraping.api.service.AmazonService;
import com.br.brenoryan.scraping.api.vo.ProductContentDetailVO;
import com.br.brenoryan.scraping.api.vo.ProductDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/amazon")
public class AmazonController {

    @Autowired
    private AmazonService amazonService;

    @GetMapping("/scrape/v3")
    public List<ProductDetailVO> scrapeWebsiteAmazonV3(@RequestParam("search") String search) {
        return amazonService.scrapeWebsiteAmazonV3(search);
    }

    @GetMapping("/scrape/content/v1")
    public ProductContentDetailVO scrapeContentWebsiteAmazon(@RequestParam("link") String link) {
        return amazonService.scrapeContentWebsiteAmazon(link);
    }

}
