package scraper;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

@Controller
public class ScraperController {

    @RequestMapping(value = "/njuskaloScraper", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Item> getData(@RequestParam(value = "query", required = true) String query, Model model) throws JSONException {
        model.addAttribute("query", query);

        String searchQuery = query;
        String baseUrl = "http://www.njuskalo.hr";

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        List<Item> itemList = new ArrayList<>();
        //JSONObject jsonToBeReturnedAsResponse = new JSONObject();

        try {
            String searchUrl = baseUrl + "?ctl=search_ads&keywords=" + URLEncoder.encode(searchQuery, "UTF-8") + "&categoryId=7";
            HtmlPage page = client.getPage(searchUrl);

            List<HtmlElement> items = page.getByXPath("//div[contains(@class, 'EntityList--Regular')]/ul/li");


            if (items.isEmpty()) {
                System.out.println("No items found");
            } else {
                for (HtmlElement htmlItem : items) {
                    HtmlAnchor itemAnchor = ((HtmlAnchor) htmlItem.getFirstByXPath(".//a"));
                    HtmlElement spanPrice = ((HtmlElement) htmlItem.getFirstByXPath(".//strong[@class='price price--hrk']"));
                    HtmlElement itemImgUrl = ((HtmlElement) htmlItem.getFirstByXPath(".//img[@data-src]"));

                    String itemPrice = spanPrice == null ? "0.0" : spanPrice.asText();

                    Item item = new Item();
                    if (itemAnchor == null) continue;
                    item.setTitle(itemAnchor.asText());
                    item.setUrl(baseUrl + itemAnchor.getHrefAttribute());
                    item.setImgUrl(itemImgUrl.getAttribute("data-src"));

                    item.setPrice(itemPrice);

                    //JSONObject jsonObject = new JSONObject();
                    //jsonObject.put("Item:", item);

                    //System.out.println(jsonObject);
                    itemList.add(item);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //jsonToBeReturnedAsResponse.put("objects", arrayOfJSONObjects);

        return itemList;
    }

}
