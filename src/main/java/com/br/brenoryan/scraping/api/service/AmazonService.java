package com.br.brenoryan.scraping.api.service;

import com.br.brenoryan.scraping.api.vo.ProductContentDetailVO;
import com.br.brenoryan.scraping.api.vo.ProductDetailVO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AmazonService {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public List<ProductDetailVO> scrapeWebsiteAmazonV3(String search){
        String link = "https://www.amazon.com.br/s?k=" + search;
        List<ProductDetailVO> products = new ArrayList<>(1000   );

        scrapAmazonWrapper(link, products);

        while (products.isEmpty()){
            scrapAmazonWrapper(link, products);
        }

        return products;
    }

    public ProductContentDetailVO scrapeContentWebsiteAmazon(String link){
        ProductContentDetailVO productContentDetail = new ProductContentDetailVO();

        scrapContentAmazonWrapper(link, productContentDetail);

        return productContentDetail;
    }

    private static void scrapAmazonWrapper(String link, List<ProductDetailVO> products) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(link)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            Elements elements = doc.select("div.s-main-slot.s-result-list.s-search-results.sg-row > div[data-component-type=s-search-result]");

            System.out.println("Quantidade de produtos encontrados: " + elements.size());

            for (Element element : elements) {
                ProductDetailVO productDetail = new ProductDetailVO();

                Element spanName = element.selectFirst("span.a-size-base-plus.a-color-base.a-text-normal");
                if (spanName != null) {
                    productDetail.setTitle(spanName.text());
                }

                Element divPrice = element.selectFirst("span.a-price-whole");
                if (divPrice != null) {
                    productDetail.setPrice(divPrice.text());
                }

                Element divHrefLink = element.selectFirst("a.a-link-normal.a-text-normal");
                if (divHrefLink != null) {
                    String productUrl = "https://www.amazon.com.br" + divHrefLink.attr("href");
                    productDetail.setUrl(productUrl);
                    extractProductDescription(client, productUrl, productDetail);
                }

                Element divImage = element.selectFirst("img.s-image");
                if (divImage != null) {
                    productDetail.setImage(divImage.attr("src"));
                }

                products.add(productDetail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void scrapContentAmazonWrapper(String link, ProductContentDetailVO productContentDetail) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(link)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            Element mainElement = doc.getElementById("ppd");

            Element itemNameElement = mainElement.selectFirst("span.a-size-large.product-title-word-break");
            if (itemNameElement != null) {
                productContentDetail.setItemName(itemNameElement.text());
            }

            ArrayList<String> imagesSrc = new ArrayList<>();
            Element imageDivElement = mainElement.getElementById("imgTagWrapperId");
            if (imageDivElement != null) {
                Element imageElement = imageDivElement.selectFirst("img");
                if (imageElement != null) {
                    String imageSrc = StringEscapeUtils.unescapeHtml4(imageElement.attr("data-a-dynamic-image"));
                    if (imageSrc != null) {
                        String jsonString = imageSrc.substring(1, imageSrc.length() - 1);
                        Pattern pattern = Pattern.compile("\"(.*?)\"");
                        Matcher matcher = pattern.matcher(jsonString);

                        while (matcher.find()) {
                            String value = matcher.group(1);
                            imagesSrc.add(value);
                        }
                    }
                }
            }
            if (!imagesSrc.isEmpty()) {
                productContentDetail.setImages(imagesSrc);
            }

            Element ratingElement = mainElement.getElementById("acrPopover");
            if (ratingElement != null) {
                String starRating = ratingElement.attr("title");
                productContentDetail.setStarRating(starRating);
            }

            Element ratingCountElement = mainElement.getElementById("acrCustomerReviewText");
            if (ratingCountElement != null) {
                String ratingCount = ratingCountElement.text();
                productContentDetail.setRatingCount(ratingCount);
            }

            Element originalPriceElement = mainElement.selectFirst("span.priceBlockStrikePriceString");
            if (originalPriceElement != null) {
                productContentDetail.setOriginalPrice(originalPriceElement.text());
            }

            Element dealPriceElement = mainElement.selectFirst("span#priceblock_dealprice");
            if (dealPriceElement == null) {
                dealPriceElement = mainElement.selectFirst("span#priceblock_ourprice");
            }
            if (dealPriceElement != null) {
                productContentDetail.setDealPrice(dealPriceElement.text());
            }

            Element videoLinkElement = mainElement.selectFirst("div#aplus_feature_div iframe");
            if (videoLinkElement != null) {
                String videoLink = videoLinkElement.attr("src");
                productContentDetail.setVideoLink(videoLink);
            }

            Element emiStartAmountElement = mainElement.selectFirst("div#pmpux_feature_div span#pmpux_feature_div_ourprice");
            if (emiStartAmountElement != null) {
                productContentDetail.setEmiStartAmount(emiStartAmountElement.text());
            }

            ArrayList<String> offersList = new ArrayList<>();
            Elements offersElements = mainElement.select("div#olp_feature_div span.a-size-base");
            for (Element offerElement : offersElements) {
                offersList.add(offerElement.text());
            }
            if (!offersList.isEmpty()) {
                productContentDetail.setOffers(offersList);
            }

            Element soldByDealerElement = mainElement.selectFirst("div#merchant-info");
            if (soldByDealerElement != null) {
                productContentDetail.setSoldByDealer(soldByDealerElement.text());
            }

            ArrayList<String> descriptionList = new ArrayList<>();
            Element descriptionElement = mainElement.selectFirst("div#featurebullets_feature_div");
            if (descriptionElement != null) {
                Elements descriptionParagraphs = descriptionElement.select("div.a-section.a-spacing-small > p > span");
                for (Element spanElement : descriptionParagraphs) {
                    descriptionList.add(spanElement.text());
                }
            }
            if (!descriptionList.isEmpty()) {
                productContentDetail.setDescription(descriptionList);
            }

            HashMap<String, String> relianceDescriptionImageDto = new HashMap<>();
            Element relianceImageElement = mainElement.selectFirst("div#altImages");
            if (relianceImageElement != null) {
                Elements relianceImages = relianceImageElement.select("img");
                for (Element img : relianceImages) {
                    relianceDescriptionImageDto.put(img.attr("alt"), img.attr("src"));
                }
            }
            if (!relianceDescriptionImageDto.isEmpty()) {
                productContentDetail.setRelianceDescriptionImageDto(relianceDescriptionImageDto);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractProductDescription(OkHttpClient client, String productUrl, ProductDetailVO productDetail) {
        Request request = new Request.Builder()
                .url(productUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            Elements descriptionElements = doc.select("div#feature-bullets > ul > li");

            ArrayList<String> descriptions = new ArrayList<>();
            for (Element descriptionElement : descriptionElements) {
                String description = descriptionElement.text().trim();
                descriptions.add(description);
            }

            productDetail.setDescription(descriptions);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
