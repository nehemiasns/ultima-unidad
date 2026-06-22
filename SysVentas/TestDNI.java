import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestDNI {
    public static void main(String[] args) {
        String url = "https://eldni.com/pe/buscar-datos-por-dni";
        try {
            Connection.Response getResponse = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .execute();
            Document doc = getResponse.parse();
            String token = doc.select("input[name=_token]").attr("value");
            
            System.out.println("Token: " + token);
            
            Connection.Response postResponse = Jsoup.connect(url)
                    .cookies(getResponse.cookies())
                    .data("_token", token)
                    .data("dni", "73944395")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();
            
            Document resultDoc = Jsoup.parse(postResponse.body());
            Element fila = resultDoc.select("table tbody tr").first();
            if (fila != null) {
                System.out.println("Found row: " + fila.text());
            } else {
                System.out.println("Row not found");
                System.out.println(resultDoc.body().text());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
