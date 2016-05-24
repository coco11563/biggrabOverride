/*package grab.importdata;

import grab.importdata.config.ConfigOperate;
import grab.importdata.dataFormat.CityData;
import grab.importdata.fileOperate.FileOperate;
import grab.importdata.fileOperate.Read;
import grab.importdata.fileOperate.StringOperate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

*//**
 * 导入数据至HBase数据库
 * 2015-10-18
 * @author licong
 *
 *//*
public class HbaseImport {
	
	private static final String columnFamily = "sinadata";
	private final static String path = "/Users/xgxy009/Documents/BDG/export";
	private final static String cityNumPath="src/grab/importdata/config/cityNum.json";
	
	private static Configuration getConfiguration() {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		conf.set("hbase.zookeeper.quorum", "192.168.1.175,192.168.1.176,192.168.1.178");
		conf.set("hbase.master", "192.168.1.177");
		return conf;
		}
	
	*//**
	 * hbase创建表方法
	 * @param tableName
	 * @param columnFamily
	 * @throws IOException
	 *//*
	public static void create(String tableName)throws IOException {
		System.out.println("创建表"+tableName);
		HBaseAdmin admin = new HBaseAdmin(getConfiguration());
		if (!admin.tableExists(tableName)) {
			@SuppressWarnings("deprecation")
			HTableDescriptor tableDesc = new HTableDescriptor(tableName);
			tableDesc.addFamily(new HColumnDescriptor(columnFamily));
			admin.createTable(tableDesc);
			}
		admin.close();
	}
	
	*//**
	 * 创建总用户表,仅需创建一次
	 * @throws IOException
	 * @throws JSONException
	 *//*
	public static void createUserTable() throws IOException, JSONException{
		create("weiboUsers");
	}
	
	*//**
	 * 创建城市用户表,仅需创建一次
	 * 创建所有城市数据表
	 * @throws IOException
	 * @throws JSONException
	 *//*
	public static void createCityUserTable() throws IOException, JSONException{
		List<String> cityList = ConfigOperate.getCity();
		JSONObject cityNumObject = new JSONObject(Read.readJson("src/config/cityNum.json"));
		for(int i=0;i<cityList.size();i++){
			String cityNum = cityNumObject.getString(cityList.get(i));
			create(cityNum+"weiboUsers");
			create(cityNum+"weiboData");
		}
	}
	
	*//**
	 * 创建当天所有城市数据表，每天导入数据时均需创建表(每天每城市一张表方案)
	 * @throws IOException
	 * @throws JSONException
	 *//*
	public static void createDataTable(String date) throws IOException, JSONException{
		List<String> cityList = ConfigOperate.getCity();
		JSONObject cityNumObject = new JSONObject(Read.readJson(cityNumPath));
		for(int i=0;i<cityList.size();i++){
			String cityNum = cityNumObject.getString(cityList.get(i));
			create(cityNum+date);
		}
	}
	
	*//**
	 * 某项是否存在
	 * @param tableName
	 * @param rowKeys
	 * @return
	 * @throws IOException
	 *//*
	@SuppressWarnings("resource")
	public static boolean ifRowExist(String tableName, String rowKeys) throws IOException{
		HTable table = new HTable(getConfiguration(), tableName);
		Get get = new Get(Bytes.toBytes(rowKeys));
		Result result = table.get(get);
		if(result.toString().contains("NONE"))
			return false;
		else 
			return true;
	}
	
	*//**
	 * 获取某行某列
	 * @param tableName
	 * @param row
	 * @param key
	 * @return
	 * @throws IOException
	 *//*
	@SuppressWarnings({ "deprecation", "resource" })
	public static String get(String tableName, String row,String key) throws IOException {
		HTable table = new HTable(getConfiguration(), tableName);
		Get get = new Get(Bytes.toBytes(row));
		Result result = table.get(get);
		for (KeyValue keyValue :result.raw()) {
			if((new String(keyValue.getQualifier())).equals(key)){
				return new String(keyValue.getValue());
			}
        }
		return "";
	}
	
	public static String changeDate(String date){
		return date.substring(0,4)+date.substring(5,7)+date.substring(8,10);
	}
	
	*//**
	 * 由文本导入数据至Hbase数据库，生成数据表和用户表
	 * @param date 2015-01-01
	 * @param path /home/sinadata
	 * @throws Exception 
	 *//*
	public static void insert(String date) throws Exception{
		List<String> cityList = ConfigOperate.getCity();
		JSONObject cityNumObject = new JSONObject(Read.readJson(cityNumPath));
		for(int i=0;i<cityList.size();i++){
			//对于每个城市
			String cityName = cityList.get(i);
			String cityNum = cityNumObject.getString(cityList.get(i));
			String cityTableName = cityNum+"weiboData";
			System.out.println("正在处理"+date+cityName+"的数据");
			List<File> fList = new ArrayList<File>();
			FileOperate.getFile(path+"/"+date+"/", cityName+".json", fList);
			if(fList.isEmpty()){
				CityData citydata = new CityData(cityNum,0);
				MySQLImport.citydataList.add(citydata);
				continue;
			}
			JSONArray json_array = Read.read_jsonFile(fList.get(0).toString());
			System.out.println(json_array.length());
			CityData citydata = new CityData(cityNum,json_array.length());
			MySQLImport.citydataList.add(citydata);
			
			@SuppressWarnings("resource")
			HTable cityTable = new HTable(getConfiguration(), cityTableName);
			ArrayList<Put> putDateList = new ArrayList<Put>();
			@SuppressWarnings("resource")
			HTable cityUserTable = new HTable(getConfiguration(), cityNum+"weiboUsers");
			ArrayList<Put> putCityUserList = new ArrayList<Put>();
			@SuppressWarnings("resource")
			HTable userTable = new HTable(getConfiguration(), "weiboUsers");
			ArrayList<Put> putUserList = new ArrayList<Put>();
			for(int j=0;j<json_array.length();j++){
				JSONObject jsonObject = json_array.getJSONObject(j);
				JSONObject geoObject = jsonObject.getJSONObject("geo");
				JSONArray coordinatesArray = geoObject.getJSONArray("coordinates");
				String lat = coordinatesArray.get(0).toString();//纬度
				String lon = coordinatesArray.get(1).toString();//经度
				
				JSONObject userObject = jsonObject.getJSONObject("user");
				String created_at = jsonObject.getString("created_at");
				String gender = userObject.getString("gender");
				String id = userObject.getString("id");
				String idstr = jsonObject.getString("idstr");
				String name = userObject.getString("name");
				String text = jsonObject.getString("text");
				String verified = userObject.getString("verified");
				String location = userObject.getString("location");
				String user_created_at = userObject.getString("created_at");
				
				Put put = new Put(Bytes.toBytes(changeDate(date).substring(2, 8)+id+idstr));
				put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("id"), Bytes.toBytes(id));
				put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("text"), Bytes.toBytes(text));
				put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("created_at"), Bytes.toBytes(created_at));
				put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("lon"), Bytes.toBytes(lon));
				put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("lat"), Bytes.toBytes(lat));
				putDateList.add(put);
				
				//总用户表
				String rowkeys2 = id;
				byte[] bRowKey2 = Bytes.toBytes(rowkeys2);
				if(!ifRowExist("weiboUsers", rowkeys2)){//如果该用户不存在，则添加新用户
					Put put1 = new Put(bRowKey2);
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("name"), Bytes.toBytes(name));
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("gender"), Bytes.toBytes(gender));
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("verified"), Bytes.toBytes(verified));
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("location"), Bytes.toBytes(location));
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("created_at"),
							Bytes.toBytes(user_created_at));
					JSONObject dataObject = new JSONObject();
					JSONArray cityArray = new JSONArray();
					cityArray.put(changeDate(date).substring(2, 8)+idstr);
					dataObject.put(cityNum, cityArray);
					
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"), 
							Bytes.toBytes(dataObject.toString()));
					putUserList.add(put1);
				}
				else{//如果该用户存在
					Put put1 = new Put(bRowKey2);
					String data = get("weiboUsers", rowkeys2, "data");
					JSONObject dataObject = new JSONObject(data);
					if(dataObject.isNull(cityNum)){
						JSONArray cityArray = new JSONArray();
						cityArray.put(changeDate(date).substring(2, 8)+idstr);
						dataObject.put(cityNum, cityArray);
					}
					else{
						JSONArray cityArray = dataObject.getJSONArray(cityNum);
						cityArray.put(changeDate(date).substring(2, 8)+idstr);
						dataObject.remove(cityNum);
						dataObject.put(cityNum, cityArray);
					}
					
					put1.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"), 
							Bytes.toBytes(dataObject.toString()));
					putUserList.add(put1);
				}
				
				//每个城市用户表
				String rowkeys3 = id;
				byte[] bRowKey3 = Bytes.toBytes(rowkeys3);
				if(!ifRowExist(cityNum+"weiboUsers", rowkeys3)){//如果该用户不存在，则添加新用户
					Put put3 = new Put(bRowKey3);
					put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("startDate"), 
							Bytes.toBytes(changeDate(date).substring(0, 6)));//记录开始月份，例如201506
					String temp = StringOperate.putIntToString(0);
					int data = Integer.valueOf(StringOperate.setOne(temp, 
							Integer.valueOf(changeDate(date).substring(6, 8))),2);
					JSONArray dataArray = new JSONArray();
					dataArray.put(data);
					put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"), 
							Bytes.toBytes(dataArray.toString()));
					putCityUserList.add(put3);
				}
				else{//如果该用户存在
					Put put3 = new Put(bRowKey3);
					String startDate = get(cityNum+"weiboUsers", rowkeys3, "startDate");
					String data = get(cityNum+"weiboUsers", rowkeys3, "data");
					JSONArray dataArray = new JSONArray(data);
					int year = Integer.valueOf(changeDate(date).substring(0, 4))
							-Integer.valueOf(startDate.substring(0, 4));
					int month = Integer.valueOf(changeDate(date).substring(4, 6))
							-Integer.valueOf(startDate.substring(4, 6))+12*year;//与记录初始月数相差月数
					int num = dataArray.length();//记录数
					if(month == 0){//num=1
						int temp = dataArray.getInt(0);
						int out = Integer.valueOf(StringOperate.setOne(StringOperate.putIntToString(temp),
								Integer.valueOf(changeDate(date).substring(6, 8))),2);
						JSONArray tempArray = new JSONArray();
						tempArray.put(out);
						if(dataArray.length()>1){
							for(int tempCount=1;tempCount<dataArray.length();tempCount++){
								tempArray.put(dataArray.getInt(tempCount));
							}
						}
						put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"),
								Bytes.toBytes(tempArray.toString()));
						putCityUserList.add(put3);
					}
					else if(month > 0){
						if(month > num-1){
							String temp = StringOperate.putIntToString(0);
							int out = Integer.valueOf(StringOperate.setOne(temp, 
									Integer.valueOf(changeDate(date).substring(6, 8))),2);
							for(int tempCount=0;tempCount<month-num;tempCount++){
								dataArray.put(0);//加0
							}
							dataArray.put(out);
							put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"),
									Bytes.toBytes(dataArray.toString()));
							putCityUserList.add(put3);
						}
						if(month == num-1){
							int temp = dataArray.getInt(dataArray.length()-1);
							int out = Integer.valueOf(StringOperate.setOne(StringOperate.putIntToString(temp),
									Integer.valueOf(changeDate(date).substring(6, 8))),2);
							JSONArray tempArray = new JSONArray();
							for(int tempCount=0;tempCount<dataArray.length()-1;tempCount++){
								tempArray.put(dataArray.getInt(tempCount));
							}
							tempArray.put(out);
							put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"), 
									Bytes.toBytes(tempArray.toString()));
							putCityUserList.add(put3);
						}
						if(month < num-1){
							String temp = StringOperate.putIntToString(0);
							int out = Integer.valueOf(StringOperate.setOne(temp, 
									Integer.valueOf(changeDate(date).substring(6, 8))),2);
							JSONArray tempArray = new JSONArray();
							tempArray.put(out);
							for(int tempCount=0;tempCount<num-month;tempCount++){
								tempArray.put(0);//加0
							}
							for(int tempCount=0;tempCount<dataArray.length();tempCount++){
								tempArray.put(dataArray.get(tempCount));
							}
							put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("data"), 
									Bytes.toBytes(tempArray.toString()));
							
							put3.add(Bytes.toBytes(columnFamily), Bytes.toBytes("startDate"), 
									Bytes.toBytes(changeDate(date).substring(0, 6)));
							putCityUserList.add(put3);
						}
					}
				}
				
				//每当数据量大于1000
				if (putDateList.size() > 1000){
					cityTable.put(putDateList);
					cityTable.flushCommits();
					putDateList.clear();
					}
				if (putCityUserList.size() > 1000){
					cityUserTable.put(putCityUserList);
					cityUserTable.flushCommits();
					putCityUserList.clear();
					}
				if (putUserList.size() > 1000){
					userTable.put(putUserList);
					userTable.flushCommits();
					putUserList.clear();
					}
			}
			
			//每当一天一地的数据处理完
			if (putDateList.size() > 0){
				cityTable.put(putDateList);
				cityTable.flushCommits();
				putDateList.clear();
				}
			if (putCityUserList.size() > 0){
				cityUserTable.put(putCityUserList);
				cityUserTable.flushCommits();
				putCityUserList.clear();
				}
			if (putUserList.size() > 0){
				userTable.put(putUserList);
				userTable.flushCommits();
				putUserList.clear();
				}
		}
	}
	
	public static void delete(String tableName) throws IOException {
		HBaseAdmin admin = new HBaseAdmin(getConfiguration());
		if (admin.tableExists(tableName)) {
			try {
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Delete " + tableName);
			}
		}
		admin.close();
		System.out.println("Delete " + tableName);
	}
	
	public static void main(String[] args) throws Exception{
//		System.out.println(StringOperate.changeDate("2015-07-01").substring(2, 8));
//		
//		JSONArray a = new JSONArray();
//		a.put(0);
//		a.put(2);
//		a.put(23);
//		a.put(10);
//		a.put(15);
//		
//		System.out.println(a.toString());
//		
//		for(int i =0 ;i<a.length();i++)
//		System.out.println(a.get(i));
//		//获取所有表
		@SuppressWarnings("resource")
		HBaseAdmin admin = new HBaseAdmin(getConfiguration());
		HTableDescriptor[] a = admin.listTables();
		for(int i=0;i<a.length;i++){
			System.out.println(a[i].getNameAsString());
//////	delete(a[i].getNameAsString());
		}
		System.out.println(a.length);
		
		insert("2015-10-22");
		MySQLImport.insert("2015-10-22");
		
		//建表，尽空数据库时运行一次
//		createUserTable();
//		createCityUserTable();
		
		
//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SS");
//		TimeZone timeZone = sdf.getTimeZone();
//		timeZone.setRawOffset(0);
//		sdf.setTimeZone(timeZone);
//		Long startTime = System.currentTimeMillis();
//		
//		insert("2015-09-01", "/Volumes/LICONG/sina");
//		
//		Long endTime1 = System.currentTimeMillis();
//		System.out.println("totalTime="+sdf.format(new Date(endTime1-startTime)));
	}

}
*/