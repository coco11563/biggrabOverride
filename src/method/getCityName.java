package method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import datastruct.KDTree;
import datastruct.KeySizeException;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class getCityName {
	/*
	 * 百度API所需要的key
	 */
	static String [] keystore = {
						  "Q9D8ftvpm5PRFvAK4gkM4HguKuVRXCHe",
						  "M9RleXI0sKd2rgMvgiQLu0LghaHAB7Zc",
						  "396489b6094a72b5a5c6c2e1c68f4c59",
						  "h7EYvV3K66Ve7ZlNgg8IqCQnlzA9Y2KI",
						  "pUav7azIRzYtBlCht6FG8hn8r4j3Stgx",
						  "pRpvlXLnN3Y0s7EHnERy73ezGq4H5LFo",
						  "8mjXttwdGncaoZxGLjGdxYzh1Q9XrQ4H",
						  "2tNp5WOMa8buj0i1gnErEnDLhOYWTfNG",
						  "nW6njT4Za5bnMGLEO8AaAfi3Wyu2ww8u0",
						  "N3kkevWBh1hTSuigNHODGmYiUsngR5EM"};
	/**
	 * 用来调用url
	 * @param url
	 * @return String from web
	 */
	private static String pcn_connUrl(String url){
		URL getUrl;
		BufferedReader br = null;
		StringBuffer buffer = new StringBuffer() ;
		try 
		{
			getUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
			connection.setConnectTimeout(30*1000);
			connection.connect();
			InputStreamReader isr = new InputStreamReader(connection.getInputStream(),"utf-8");
			br = new BufferedReader(isr); 
			int s;
			while((s = br.read())!=-1)
			{
				buffer.append((char)s);
			}
		}
		catch (IOException e) 
		{
			System.out.println ("error" );
		}
		return  buffer.toString() ;	
	}
	/**
	 * 从json中解析出坐标
	 * 然后根据这个坐标调用百度API返回城市名和省市名
	 * @param jsonObject
	 * @return String[] 省市名
	 * @throws IOException
	 */
	private static String[] pcn_getProCityNameURL(JSONObject jsonObject) throws  IOException
	{
		String[]			cityName			=		null;
		try
		{
			double 				location[] 			= 		pcn_getCoordinates( jsonObject ) ;	
//			String 				url						=		"http://api.map.baidu.com/geocoder?location="+location[0]+","+location[1]+"&output=json&key=28bcdd84fae25699606ffad27f8da77b" ;
			String  			url						=  		"http://api.map.baidu.com/geocoder/v2/?ak=Q9D8ftvpm5PRFvAK4gkM4HguKuVRXCHe&location="+location[0]+","+location[1]+"&output=json&coordtype=gcj02ll";
			String 				baiduLocation 	= 		pcn_connUrl(url) ;
			JSONObject 		temp_object		=		new JSONObject(baiduLocation) ;
			temp_object		= 		temp_object.getJSONObject("result") ;
			temp_object		= 		temp_object.getJSONObject("addressComponent") ;
			cityName			=		new String[2] ;
			cityName[0]		=		temp_object.getString("province") ;
			cityName[1]		=		temp_object.getString("city") ;
			
			if(cityName[1].contains ( "直辖县级行政单位" ) )
				cityName[1]	=		temp_object.getString("district") ;
			
			System.out.println ( location[0]+"\t"+location[1] + ":\t" + cityName[0]+"-"+cityName[1]);
			return cityName;
		}
		catch(Exception e)
		{
			cityName		=		new String[2] ;
			cityName[0]	=	"Exception" ;
			cityName[1]	=	"Exception" ;
			return cityName;
		}
	}
	/**
	 * 输入坐标，返回省市名
	 * @param lat
	 * @param lng
	 * @return 省市名 0province 1city
	 * @throws IOException
	 */
	public static String[] pcn_getProCityNameURL(double [] location , int index) throws  IOException
	{
		
		String[]			cityName			=		null;
		String 				key 				= keystore[index];
		if(index > 9)
		{
			cityName			=		new String[2] ;
			cityName[0] = "exception province";
			cityName[1] = "exception city";
			System.out.println ( location[0]+"\t"+location[1] + ":\t" + cityName[0]+"-"+cityName[1]);
			return cityName;
		}
		try
		{
//			String 				url						=		"http://api.map.baidu.com/geocoder?location="+location[0]+","+location[1]+"&output=json&key=28bcdd84fae25699606ffad27f8da77b" ;
			String  			url						=  		"http://api.map.baidu.com/geocoder/v2/?ak="+key+"&location="+location[0]+","+location[1]+"&output=json&coordtype=gcj02ll";
			String 				baiduLocation 	= 		pcn_connUrl(url) ;
			JSONObject 		temp_object		=		new JSONObject(baiduLocation) ;
			temp_object		= 		temp_object.getJSONObject("result") ;
			temp_object		= 		temp_object.getJSONObject("addressComponent") ;
			cityName			=		new String[2] ;
			cityName[0]		=		temp_object.getString("province") ;
			cityName[1]		=		temp_object.getString("city") ;
			
			if(cityName[1].contains ( "直辖县级行政单位" ) )
				cityName[1]	=		temp_object.getString("district") ;
			
			System.out.println ( location[0]+"\t"+location[1] + ":\t" + cityName[0]+"-"+cityName[1]);
			return cityName;
		}
		catch(Exception e)
		{
			
			cityName		=		pcn_getProCityNameURL(location , index++) ;
			return cityName;
		}
	}
	/**
	 * 负责从输入的jsonobj中解析出地理坐标
	 * @param json_object
	 * @return 经纬度坐标
	 * @throws JSONException
	 */
	private static double[] pcn_getCoordinates(JSONObject json_object) throws JSONException
	{
		JSONObject 	geo_object 		= 		json_object.getJSONObject ( "geo" ) ;
		JSONArray		coor_jsarray		=		geo_object.getJSONArray ( "coordinates" ) ;
		double 			location[] 			= 		new double[2] ;
		location[0]		=		coor_jsarray.getDouble ( 0 ) ;
		location[1]		=		coor_jsarray.getDouble ( 1 ) ;
		return location ;
	}
	
	public  static void main(String args[]) throws IOException
	{
		String[]			cityName			=		null;
		System.out.println(cityName[0]);
	}
	/**
	 * 
	 * @param kdtree
	 * @param json_object
	 * @return 0 province 1 city
	 * @throws JSONException
	 */
	public static String[] KnnCity(KDTree<point> kdtree ,JSONObject json_object) throws JSONException
	{
		double[] coordinary = pcn_getCoordinates(json_object);
		String[] city = new String[2] ;
		try {
			point cityimform = kdtree.nearest(coordinary);
			city[0] = cityimform.getProvince();
			city[1] = cityimform.getCity();
		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			city[0] = "wrongcoor";
			city[1] = "wrongcoor";
		}
		return city;
		
		
	}
	
}
