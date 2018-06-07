package caerux.tinh.news;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class News {


    private static final String USER_AGENT = "Mozilla/5.0";

    private static final String USER_ID = "1d5326ced13743c682fc62affcc29be3";

    private static final String GET_URL = "https://newsapi.org/v2/top-headlines?country=jp";


    private static String[] unit = {"general", "sports"};
    private static ArrayList<String> categories = new ArrayList<>();


    public static void main(String[] args) throws IOException {


//        categories.add("ドル");
//        categories.add("日本円");
//        System.out.println(unit[categories.indexOf("ドル")]);
//        System.out.println(unit[categories.indexOf("日本円")]);
        System.out.println(getNews("スポーツ"));
    }

    public static String getNews(String category) throws IOException {

        categories.add("全部");
        categories.add("スポーツ");
        URL obj;
        if (categories.contains(category)){
            obj = new URL(GET_URL.concat("&category=").concat(unit[categories.indexOf(category)]).concat("&apiKey=").concat(USER_ID));
            System.out.println("URL :: " + obj.toString());
        }
        else {
            return "Category unsupported";
        }
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(response.toString());
                int totalResults = (int)jsonObj.get("totalResults");
                JSONArray articles = jsonObj.getJSONArray("articles");
                StringBuffer sb = new StringBuffer();
                sb.append("次は、").append(LocalDate.now().toString()).append("のニュースです。");
                for (int i = 0; i < 1; i++) {
                    JSONObject article = articles.getJSONObject(i);
                    String name = article.getString("author");
                    String title = article.getString("title");
                    String description = article.getString("description");
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(name).append("によって").append(title).append(description);
                    sb.append(buffer.toString());
                }
                sb.append("。今日のニュースはここで終わります");
                return sb.toString();
            } catch (JSONException e) {
                return "エラー　" + e.toString();
            }
//            return response.toString();
        } else {
            System.out.println("GET request not worked");
            return "エラー　" + category + responseCode;
        }

    }
    private static final Map<String, String> units = new HashMap<>();

    static {
        units.put("USD", "ドル");
        units.put("JPY", "日本円");
        units.put("EUR", "ユーロ");
        units.put("GBP", "英ポンド");
        units.put("CNY", "人民元");
    }
    public static String get(String item) {
        return units.get(item);
    }
}

