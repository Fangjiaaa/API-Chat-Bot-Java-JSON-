
package WeatherBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.PircBot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class WeatherBot extends PircBot {
 
    static final String server = "irc.freenode.net";
    static final String channel = "#fangjia";
    static String defaultlocation = "75019";
    public WeatherBot(){
        this.setName("Myweatherbot");
        this.isConnected();
        
    }
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        
        
        message = message.toLowerCase();
        
        //for weather 
        Pattern p = Pattern.compile(".*([0-9][0-9][0-9][0-9][0-9])");
        Matcher zip = p.matcher(message);
        Pattern p2=Pattern.compile(".*(news)");
        Matcher news = p2.matcher(message);
        String location = "";
        boolean printed=false;
        String[] s = message.split(" ");     
        if(news.find())
        {
        	printed=true;
        	TopNews temp[] = NewsService.getNews();
        	
        	for(int i = 0; i<4;i++)
        	{
        	sendMessage(channel, temp[i].getTitle());
        	sendMessage(channel, temp[i].getUrl());
        	String line=String.format("==========News: %d==========",i+1);
        	sendMessage(channel, line);
        	}
        	sendMessage(channel, "Powered by News API");
        	sendMessage(channel, "https://newsapi.org/");
        } 
   
   
        if (zip.find()){                //if contains zipcode, then it is location
            location = zip.group(1);
        }else if(message.contains("in")){        //the word after "in" is location
            StringBuilder temp=new StringBuilder (message);
            for(int i=0;i<temp.length();i++)
            {
            	if((temp.charAt(i)>='0'&&temp.charAt(i)<='9')||(temp.charAt(i)>='a'&&temp.charAt(i)<='z')||(temp.charAt(i)>='A'&&temp.charAt(i)<='Z')||(temp.charAt(i)==' '));
	        	else{
	        		temp.deleteCharAt(i--);
            	}
            }
            String NewMessage=temp.toString();
        	String segments[] = NewMessage.split("in ");
            location = segments[segments.length - 1];
        }else if(s.length == 2){                 //if "weather location" or "location weather", we can get the location 
            if (s[0].equals("weather")) {
                    location = s[1];
                } else {
                    location = s[0];
                }
        }else{
        	if(printed==false)
        	{
            sendMessage(channel, "Sorry, I don't understand.");
            sendMessage(channel, "you can ask me about weather or news.");
            sendMessage(channel, "Example: \"Weather in San Francisco\" or \"Show me some news\".");
            sendMessage(channel, "By the way, I'm not able to find local news");
                

        	}
        }
        if (location!="")
        {
        	location  = location + ",US";     //I added "US" because q="location ", and the location must be "zipcode, US" or "cityname, US"
        	Weather data = WeatherService.getWeather(location); //get weather information from WeatherService using location
        	sendMessage(channel, data.toString());   //bot display weather information 
        }
        
        
       
    }
    

public static void main(String[] args) throws Exception{    //try to connect to freenode, but it only works on freenode chat page, does not run locally 
    
    WeatherBot bot = new WeatherBot();
    bot.setVerbose(true);
    bot.connect("irc.freenode.net",6667);
    bot.joinChannel(channel);
    bot.sendMessage(channel, "Hi, welcome to weatherbot.");
  
}
}

class Weather{                           //datas 
    private double temp;
    private double min;
    private double max;
    private int wind;
    private int clouds;
    private String weather;
    private String location;

    public Weather(double temp, double min, double max, int wind, int clouds, String weather, String location) {
        this.temp = temp;
        this.min = min;
        this.max = max;
        this.wind = wind;
        this.clouds = clouds;
        this.weather = weather;
        this.location = location;
    }

    public double getTemp() {
        return temp;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getWind() {
        return wind;
    }

    public int getClouds() {
        return clouds;
    }

    public String getWeather() {
        return weather;
    }

    public String getLocation() {
        return location;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setWind(int wind) {
        this.wind = wind;
    }

    public void setClouds(int clouds) {
        this.clouds = clouds;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    //print weather infromation (you can change this part)
    public String toString(){ 
        return String.format("%s, %.1f C in %s, temperature from %.1f to %.1f, wind %d m/s. clouds %d percent. " , weather, temp, location, min, max, wind, clouds);
    }   
}
class TopNews{                           //datas 
    private String title;
    private String description;
    private String url;
    public TopNews()
    {
    	title="";
    	description="";
    	url="";
    }
    
    public TopNews(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;

    }
    public TopNews(String title, String url) {
        this.title = title;
        this.url = url;

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    //print weather infromation (you can change this part)
    public String toString(){ 
    	String result="/n"+title+"/n"+description+"/n"+url;
        return result;
    }   
}



class NewsService{
	    						
	public static TopNews[] getNews() {
		
		try {
			JsonParser parser=new JsonParser();
			final URL url = new URL("https://newsapi.org/v2/top-headlines?country=us&apiKey=6900f651bd9f49a28937ff2c038d3d04");
			HttpURLConnection http = (HttpURLConnection) url.openConnection();//send get request to api 
			http.setRequestMethod("GET");// Make a GET request to the API 
			BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream())); //http.getInputStream() is the data from the api, and InputStreamReader can read the data
			JsonObject object = (JsonObject) parser.parse(reader);
			JsonArray array = object.get("articles").getAsJsonArray();
			TopNews result [] = new TopNews [4];
			
			for (int i =0;i<4;i++)
			{
				JsonObject subObject = array.get(i).getAsJsonObject();
				String title = " ";
				String theUrl = " ";
				if(subObject.get("title").getAsString()!=null)
					title = subObject.get("title").getAsString();
				if(subObject.get("url").getAsString()!=null)
					theUrl = subObject.get("url").getAsString();
				
				result [i]=new TopNews(title,theUrl);
			}
			
			reader.close();
			return result;
			}catch (Exception e) {
			System.out.println(e);
			}
		return null;
	}

}


class WeatherService {

    
    private static final String key = "3150e5ba15662367756b387d1abb0253";

    // API endpoint for getting weather data
    private static final String endpoint = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&APPID=%s";//Appid= key, q= location 
    

    
    public static Weather getWeather(String location) {
        try {
            // Make a GET request to the API
            URL url = new URL(String.format(endpoint, location, key));//q =location and APPID = key
            HttpURLConnection http = (HttpURLConnection) url.openConnection(); 
            http.setRequestMethod("GET"); //send get request to api
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream())); //http.getInputStream() is the data from the api, and InputStreamReader can read the data 
            StringBuilder result = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            // Return the weather information from parsJSON
            return parseJSON(result.toString());

        } catch (Exception e) {
            System.out.println("Failed to fetch weather data");
        }
        
        return null;
    }

    //get weather information from Json 
    private static Weather parseJSON(String json) {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();

        // objects under main(temprautre, max temp and min temp
        JsonObject main = object.getAsJsonObject("main");

        double temp = main.get("temp").getAsDouble();
        double max = main.get("temp_max").getAsDouble();
        double min = main.get("temp_min").getAsDouble();
        
        //get wind speed inforamtion from wind object 
        JsonObject winds = object.getAsJsonObject("wind");
        int wind = winds.get("speed").getAsInt();
        
        //get cloud percent from cloud object
        JsonObject cloud = object.getAsJsonObject("clouds");
        int clouds = cloud.get("all").getAsInt();

        // Get weather from the weather array
        String weather = object.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

        // get location t
        String location = object.get("name").getAsString();

        
        return new Weather(temp, min, max, wind, clouds, weather, location);
    }

}