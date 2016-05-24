package grab.importdata.config;

import grab.importdata.fileOperate.Read;
import grab.importdata.fileOperate.Write;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

/**
 * 
 * @author licong
 *
 */
public class ConfigOperate {
	private final static String areaPath = "/home/biggrab/config/getServicesSuportArea.json";
	private final static String cityNumPath = "/home/biggrab/config/cityNum.json";
	private final static String provinceNumPath = "/home/biggrab/config/province_data.json";
	
	/**
	 * 获取城市名列表
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<String> getCity() throws IOException, JSONException{
		List<String> cityList = new ArrayList<String>();
		JSONArray areaArray = new JSONArray(Read.readJson(areaPath));
		for(int i=0;i<areaArray.length();i++){
			JSONObject province = areaArray.getJSONObject(i);
			JSONArray city = province.getJSONArray("c");
			for(int j=0;j<city.length();j++){
				cityList.add(city.getString(j));
			}
		}
		return cityList;
	}
	
	/**
	 * 产生城市对应编号
	 * @throws JSONException
	 */
	public static void createCityNumFile() throws JSONException{
		JSONObject cityObject = new JSONObject();
		
		JSONArray areaArray = new JSONArray(Read.readJson(areaPath));
		for(int i=0;i<areaArray.length();i++){
			JSONObject province = areaArray.getJSONObject(i);
			String provinceNum;
			if(i<10)provinceNum="0"+i;
			else provinceNum = String.valueOf(i);
			JSONArray city = province.getJSONArray("c");
			for(int j=0;j<city.length();j++){
				String cityNum;
				if(j<10)cityNum="0"+j;
				else cityNum = String.valueOf(j);	
				cityObject.put(city.getString(j), provinceNum+cityNum);
			}
		}
		Write.writeJson("src/config/", cityObject, "cityNum");
	}
	
	public static String getCityNum(String city) throws JSONException, IOException{
		if(city.equals("襄樊市"))return "2112";
		List<String> cityList = ConfigOperate.getCity();
		JSONObject cityNumObject = new JSONObject(Read.readJson(cityNumPath));
		for(int i=0;i<cityList.size();i++){
			if(cityList.get(i).equals(city)){
				return  cityNumObject.getString(cityList.get(i));
			}
		}
		return "error";
	}
	
	public static String getProvinceNum(String city) throws JSONException, IOException{
		JSONObject cityNumObject = new JSONObject(Read.readJson(provinceNumPath));
		for(int i=0;i<34;i++){
			String province;
			String id;
			if(i<10)
				id = "0"+i;
			else
				id = Integer.toString(i);
			province=cityNumObject.getString(id);
			if(province.contains(city))return id;
		}
		return "error";
	}
	
	public static String getProvince(String id) throws JSONException, IOException{
		JSONObject cityNumObject = new JSONObject(Read.readJson(provinceNumPath));
		return cityNumObject.getString(id);
	}
	
	public static void main(String[] args) throws IOException, JSONException{
//		List<String> cityList = getCity();
//		for(String a : cityList)System.out.println(a);
//		createCityNumFile();
		System.out.println(getProvinceNum("北京"));
	}
}
