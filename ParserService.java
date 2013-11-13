

import org.jsoup.*;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ParserService {
    /**
     *  The Query1 would connect to "www.shopping.com" and return the total result number of the search word
     * @param keyWord        The Search word
     */

    public static int Query1(String keyWord){
        String midKeyWord = keyWord.replace(" ", "-");
        String endKeyWord = keyWord.replace(" ", "+");
        //the pattern url is based on real url
        String url =  "http://www.shopping.com/" + midKeyWord + "/products?CLT=SCH&KW=" + endKeyWord;
        //the number of results of the search word
        int result = 0;
        try {
            Document doc = Jsoup.connect(url).get();
            // The total number is hidden in this element <span name="NumItemsReturned:1496"></span>
            Elements number = doc.select("span[name]");
            if (number == null) {
                return 0;
            }
            else {
                for (Element src : number){
                    String[] value = src.attr("name").split(":");
                    if(value[0].equals("NumItemsReturned")){
                        result = Integer.parseInt(value[1]);

                    }
                }
                return result;
            }
        } catch (IOException e){
            System.out.println("IOException");
            return 0;
        }

    }

    /**
     *  The Query2 would connect to "www.shopping.com" and return the list of results that on the given page number of search result
     * @param keyWord        The Search word
     * @param pageNum        The page number of the research result
     */
    public static ArrayList<ResultObject> Query2(String keyWord, int pageNum){
        ArrayList<ResultObject> result = new ArrayList<ResultObject>();
        String url = pageUrl(keyWord, pageNum);
        try {
            Document doc = Jsoup.connect(url).get();
            //every item on the page have a quick look id stored in <div class="gridBox deal  feature" id="quickLookItem-1">
            Elements items = doc.getElementsByAttributeValueStarting("id", "quickLookItem");

            int n = 0; // the count of the result object
            for(Element product: items) {
                ResultObject temp = new ResultObject();
                n++;
                // The title is stored in <span id="nameQA1" title="COBRA DIGITAL DC8650RD 8.0 Megapixel 3-in-1 Digital Camera (Red)">COBRA
                // <b class='highlight'>DIGITAL</b> DC8650RD 8.0... </span>
                Element name = product.getElementById("nameQA"+ n);
                temp.title = name.attr("title");

                // The price is stored in <input type="hidden" name="itemPrice" value="32.95"/>
                Elements price = product.getElementsByAttributeValue("name", "itemPrice");
                temp.price = price.get(0).attr("value");

                //The shipping price has three situation
                //It is possible to be free shipping stored in  <div class="freeShip">Free Shipping</div>
                //It is possible have a specific shipping price stored in <span class="calc"> + &#36;5.74 shipping </span>
                //It is also possible there is no shipping information on the page
                Elements freeShip = product.getElementsByClass("freeship");
                if (!freeShip.isEmpty()){
                    temp.ship = freeShip.text();
                }
                else {
                    Elements shipPrice = product.getElementsByClass("calc");
                    if (!shipPrice.isEmpty()) {
                        String realPrice = shipPrice.get(0).text().split(" ")[1];
                        temp.ship = realPrice;
                    }
                    else {
                        temp.ship = "Not available";
                    }
                }

                //The vendor has two situations
                //If there is multiple stores, the number is stored in <span id="numStoresQA2">2 stores</span>
                //If there is only one store, the name is stored in <span id="merchNameQA1" class="merchantName  unclickable">Amazon</span>
                Element vendor = product.getElementById("numStoresQA" + n);
                if (vendor != null){
                    temp.vendor = vendor.text();
                }
                else {
                    temp.vendor = product.getElementById("merchNameQA" + n).text();
                }

                result.add(n-1, temp);
            }
            return result;

        } catch (IOException e) {
            System.out.println("IOException");
            return result;
        }

    }

    /**
     *  The helper function for the Query2 to find the Url.
     *  The first page have a different pattern from the other pages
     * @param keyWord        The Search word
     * @param pageNum        The page number of the research result
     */

    public static String pageUrl (String keyWord, int pageNum){
        String midKeyWord = keyWord.replace(" ", "-");
        String endKeyWord = keyWord.replace(" ", "+");
        if(pageNum < 1) {
            System.out.println("Please input valid pageNum");
            return null;
        }
        else if (pageNum == 1){
            String url = "http://www.shopping.com/" + midKeyWord + "/products?CLT=SCH&KW=" + endKeyWord;
            return url;
        }
        else {
            String url = "http://www.shopping.com/" + midKeyWord + "/products~PG-" + pageNum + "?KW=" + endKeyWord;
            return url;

        }
    }


    public static void main(String[] args) {

        if (args.length == 0 || args.length > 2) {
            System.out.println("Please input valid argument.");
            return;
        }

        if (args.length == 1) {
            int result = Query1(args[0]);
            if(result == 0) {
                System.out.println("There is no result match " + args[0] + " on shopping.com");
            }
            else {
                System.out.println("The total number of result is: " + result);
            }
        }

        if (args.length == 2) {
            int pageNum = Integer.parseInt(args[1]);
            ArrayList<ResultObject> products = Query2(args[0], pageNum);
            if(products.isEmpty()){
                System.out.println("There is no result match" + args[0] + " on page" + pageNum);
            }
            for(ResultObject e: products){
                System.out.println("Product Title: " + e.title);
                System.out.println("Price: $" + e.price);
                System.out.println("shipping: " + e.ship);
                System.out.println("vendor: " + e.vendor);


            }

        }
    }



}
