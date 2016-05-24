package grab.importdata;

import grab.importdata.dataFormat.CityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author licong
 * 2015-10-18
 * MySQL数据库操作,数据库建表
 */
public class MySQLImport {
	/**
	 * 存储每天数据的List
	 */
	public static List<CityData> citydataList=new ArrayList<CityData>();;
	/**
	 * 获取数据库连接
	 * @return Connection对象
	 */
	public static Connection getConnection(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.1.170:3307/geoweibo?characterEncoding=GBK";
			conn = DriverManager.getConnection(url, "root", "1234");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接
	 * @param conn Connection对象
	 */
	public static void closeConnection(Connection conn){
		// 判断conn是否为空
		if(conn != null){
			try {
				conn.close();	// 关闭数据库连接
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 导入数据至mySql数据库，请先添加citydataList!!
	 * @param date format:2015-01-01
	 * @throws SQLException
	 */
	public static void insert(String date) throws SQLException{
		Connection conn = getConnection();
		
		//导入城市数量数据
		String insertCityDataSql = "insert into weibo_count (date";
		for(int j=0;j<citydataList.size();j++){
			insertCityDataSql=insertCityDataSql+",city"+citydataList.get(j).getCityNum();
		}
		insertCityDataSql=insertCityDataSql+") values(?";
		for(int j=0;j<citydataList.size();j++){
			insertCityDataSql=insertCityDataSql+",?";
		}
		insertCityDataSql=insertCityDataSql+")";
		PreparedStatement ps = conn.prepareStatement(insertCityDataSql);
		ps.setString(1, date);
		for(int j=0;j<citydataList.size();j++){
			ps.setInt(j+2, citydataList.get(j).getCount());
		}
		ps.executeUpdate();
		ps.close();
		
		//导入省数量数据
		List<CityData> provinceList = new ArrayList<CityData>();
		for(int i=0;i<34;i++){
			String provinceNum;
			int provinceCount=0;
			if(i<10)provinceNum="0"+i;
			else provinceNum=String.valueOf(i);
			for(int j=0;j<citydataList.size();j++){
				if(citydataList.get(j).getCityNum().startsWith(provinceNum))
					provinceCount=provinceCount+citydataList.get(j).getCount();
			}
			CityData provinceData = new CityData(provinceNum,provinceCount);
			provinceList.add(provinceData);
		}
		String insertProvinceDataSql = "insert into province_count (date";
		for(int j=0;j<provinceList.size();j++){
			insertProvinceDataSql=insertProvinceDataSql+",province"+provinceList.get(j).getCityNum();
		}
		insertProvinceDataSql=insertProvinceDataSql+") values(?";
		for(int j=0;j<provinceList.size();j++){
			insertProvinceDataSql+=",?";
		}
		insertProvinceDataSql+=")";
		PreparedStatement ps2 = conn.prepareStatement(insertProvinceDataSql);
		ps2.setString(1, date);
		for(int j=0;j<provinceList.size();j++){
			ps2.setInt(j+2, provinceList.get(j).getCount());
		}
		ps2.executeUpdate();
		ps2.close();
		
		citydataList.clear();
		conn.close();
	}
	
	/**
	 * 创建表
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void progressSql(String sql,String table_name) throws SQLException, ClassNotFoundException{
		Connection conn = getConnection();
		ResultSet rs = conn.getMetaData().getTables(null, null, table_name, null );
		if(!rs.next()){
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
		}
	}
	
	/**
	 * 从数据库中取数据
	 * @param sql SQL语句
	 * @param num 选取的第几个字段(>1)
	 * @return 该字段名的全部数据String[]
	 * @throws SQLException 
	 */
	public static List<String> selectDistinctData(String sql,int num) throws SQLException{
		LinkedList<String> dataList = new LinkedList<String>();
		Connection conn = getConnection();
		Statement stmt=conn.createStatement();
		ResultSet rs=stmt.executeQuery(sql);
		while (rs.next()) {
			String idString = rs.getString(num);
			if(dataList.size()==0){
				dataList.add(rs.getString(num));
			}
			else{
				for(int i=0;i<dataList.size();i++){
					if(dataList.get(i).equals(idString))break;
					else if(i==dataList.size()-1){
						dataList.add(rs.getString(num));
					}
				}
			}
		}		
        rs.close();
        stmt.close();
        conn.close();
		return dataList;
	}
	

	/**
	 * 从数据库中取数据
	 * @param sql SQL语句
	 * @param num 选取的第几个字段(>1)
	 * @return 该字段名的全部数据String[]
	 * @throws SQLException 
	 */
	public static List<String> selectData(String sql,int num) throws SQLException{
		LinkedList<String> dataList = new LinkedList<String>();
		Connection conn = getConnection();
		Statement stmt=conn.createStatement();
		ResultSet rs=stmt.executeQuery(sql);
		while (rs.next()) {
			dataList.add(rs.getString(num));
		}		
        rs.close();
        stmt.close();
        conn.close();
		return dataList;
	}
	
	/**
	 * 链接URL返回string
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String connUrl(String url) throws IOException
	{
		URL getUrl;
		try 
		{
			getUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
			connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// ����
			String jason_data = "";
			while ((jason_data = reader.readLine()) != null)
			{
				return jason_data;
			}
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			return "error";
		}
		return "error";	
	}

	public static void main(String[] args) throws SQLException{
		if("00123".startsWith("0012")){System.out.println("yes");}
		
		int m=9;
		int n=(++m>>1);
		System.out.println(n);
	}
}
