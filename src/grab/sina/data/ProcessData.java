
package grab.sina.data;


import grab.sina.config.readConfig;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 提供一个统计抓取数据过程中，对数据进行处理的类
 * 
 * 项目名称: BigDataGrab
 * 
 * 包: grab.sina.data
 * 
 * 类名称: ProcessData
 * 
 * 类描述: 提供一个统计抓取数据过程中，对数据进行处理的类
 * 
 * 创建人: 李佳骏
 * 
 * 创建时间: 2014年11月18日
 * 
 * 修改人: 无
 * 
 * 修改时间: 无
 * 
 * 修改备注: 无
 * 
 * 版本: [v1.0]
 * 
 */
public class ProcessData
{
	
	/**
	 * 
	 * 组织https://api.weibo.com/2/place/nearby_timeline.json?接口的URL
	 * 
	 * 添加时间：2014年11月22日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param access_token
	 * 
	 * @param lat
	 * 
	 * @param lon
	 * 
	 * @param unix_end_time
	 * 
	 * @param unix_start_time
	 * 
	 * @param area_range
	 * 
	 * @param count
	 * 
	 * @param page
	 * 
	 * @return
	 * 
	 */
	public static String url_nearby_timeline ( String access_token, double lat,
			double lon, long unix_end_time, long unix_start_time,
			double area_range, int count, int page )
	{
		String URL = "https://api.weibo.com/2/place/nearby_timeline.json?"
				+ "access_token=" + access_token + "&lat=" + lat + "&long="
				+ lon + "&endtime=" + unix_end_time + "&starttime="
				+ unix_start_time + "&range=" + area_range + "&count=" + count
				+ "&page=" + page;
		return URL;
	}

	/**
	 * @功能 把标准时间格式转化成UNIX时间戳。每10秒加10，有进位
	 * @格式 "yyyy-MM-dd HH:mm:ss"
	 * @返回类型 long
	 * @param time
	 * @return
	 */
	public static long unixTime ( String time )
	{
		try
		{
			// String time = "2013-04-20 00:00:00";
			Calendar date_start = Calendar.getInstance ( );
			Date date = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" )
					.parse ( time );
			long unixTimestamp = date.getTime ( ) / 1000;
			// System.out.println(unixTimestamp);
			return unixTimestamp;
		}
		catch ( ParseException e )
		{
			e.printStackTrace ( );
		}
		return -1;
	}

	/**
	 * 
	 * @param itemJsonObject
	 * @return 返回经纬度值
	 * @throws JSONException
	 */
	public static double [ ] getGeo ( JSONObject itemJsonObject )
			throws JSONException
	{
		String geo = itemJsonObject.getString ( "geo" );
		JSONObject geoObject = new JSONObject ( geo );
		String coordinates = geoObject.getString ( "coordinates" );
		String locationString[] = coordinates.split ( "," );
		double location[] = new double [ 2 ];
		location [ 0 ] = Double.valueOf ( locationString [ 0 ].substring ( 1,
				locationString [ 0 ].length ( ) ) );
		location [ 1 ] = Double.valueOf ( locationString [ 1 ].substring ( 0,
				locationString [ 1 ].length ( ) - 1 ) );
		geoObject = null;
		return location;
	}

	/**
	 * 连接URL，并返回取到的数据
	 * 
	 * 添加时间：
	 * 
	 * 添加人：
	 * 
	 * 修改时间：2014年11月22日
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改内容：添加超时响应机制，避免出现链接卡死。
	 * 
	 * @param url
	 *            传入的URL
	 * @return 返回的数据
	 */
	public static String connUrl ( String url ) throws IOException
	{
		URL getUrl;
		try
		{
			getUrl = new URL ( url );
			HttpURLConnection connection = (HttpURLConnection) getUrl
					.openConnection ( );
			connection.setReadTimeout(20 * 1000); 
			connection.connect ( );
			BufferedReader reader = new BufferedReader ( new InputStreamReader (
					connection.getInputStream ( ), "utf-8" ) );// 设置
			String jason_data = "";
			while ( ( jason_data = reader.readLine ( ) ) != null )
			{
				return jason_data;
			}
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			return "error";
		}
		return "error";
	}

	/**
	 * 根据经纬度，提供Web墨卡托的分块块号。
	 * 
	 * @param lon
	 * @param lat
	 * @param zoom
	 * @return
	 */
	static public String wkt_code ( double lon, double lat, int zoom )
	{
		// lon：经
		// lat：纬
		double web_mkt[] = wkt_lonLat2Mercator ( lon, lat );
		double x = web_mkt [ 0 ] + 20037508.342789;
		double y = 20037508.342789 - web_mkt [ 1 ];
		double res = wkt_getZoomRes ( zoom );
		double spanX = res * 256;// zu[0]为1px代表的纬度
		double spanY = res * 256;// zu[1]为1px代表的经度
		int bx = (int) ( x / spanX );
		int by = (int) ( y / spanY );
		return zoom + "_" + bx + "_" + by;
	}

	/**
	 * wkt_code的子函数：计算当前缩放级别的分辨率
	 * 
	 * @param zoom
	 * @return
	 */
	static private double wkt_getZoomRes ( int zoom )
	{
		double tileSize = 256; // 瓦片尺寸(256*256)
		double initialResolution = 2 * Math.PI * 6378137 / tileSize; // 6378137为球体半径
		double res = initialResolution / Math.pow ( 2, zoom ); // zoom为层数(0-21)
		return res;
	}

	/**
	 * wkt_code的子函数：w84转为墨卡托坐标(经纬度转墨卡托)，坐标原点在左上角
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	static private double [ ] wkt_lonLat2Mercator ( double lon, double lat )
	{
		double [ ] xy = new double [ 2 ];
		double x = lon * 20037508.342789 / 180;
		double y = Math.log ( Math.tan ( ( 90 + lat ) * Math.PI / 360 ) )
				/ ( Math.PI / 180 );
		y = y * 20037508.34789 / 180;
		xy [ 0 ] = x;
		xy [ 1 ] = y;
		return xy;
	}

	/**
	 * 从 Unicode 形式的字符串转换成对应的编码的特殊字符串。 如 "\u9EC4" to "黄". Converts encoded
	 * \\uxxxx to unicode chars and changes special saved chars to their
	 * original forms
	 * 
	 * @return 完成转换，返回编码前的特殊字符串。
	 */
	public static String encoding_UnicodeToCHN ( String str )
	{
		Pattern pattern = Pattern.compile ( "(\\\\u(\\p{XDigit}{4}))" );
		Matcher matcher = pattern.matcher ( str );
		char ch;
		while ( matcher.find ( ) )
		{
			ch = (char) Integer.parseInt ( matcher.group ( 2 ), 16 );
			str = str.replace ( matcher.group ( 1 ), ch + "" );
		}
		return str;
	}

	/**
	 * 汉字转Unicode
	 * 
	 * @param s
	 * @return
	 */
	public static String encoding_CHNToUnicode ( final String s )
	{
		String str = "";
		for ( int i = 0; i < s.length ( ); i++ )
		{
			int ch = (int) s.charAt ( i );
			str += "\\u" + Integer.toHexString ( ch );
		}
		return str;
	}
	
	/**
	 * 获取到下一个时刻所需要的秒数。
	 * @return
	 */
	public static long getSecondsToNextClockSharp()
	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, 1);
		//c.set(Calendar.HOUR, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return (c.getTime().getTime() - System.currentTimeMillis())/1000;
	}
	

	/**
	 * 
	 * 获取一条新浪微博发送的省、市名称。
	 * 数据本身有两种情况：1.已经提供省、市名字；2.没有提供省、市名字。
	 * 情况1：直接解析获取；情况2：调用百度API获取
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param json_object
	 * 
	 * @return
	 * 
	 * @throws JSONException 
	 * 
	 * @throws IOException 
	 * 
	 */
	public static String[] pcn_getProCityName( JSONObject json_object ) throws JSONException, IOException
	{		
		/*
		String address = pcn_haveProCityName ( json_object );
		if( !address.equals ( "false" ) )
			return pcn_dealSpecialProCityName ( pcn_getAddressObjectProCityName ( new JSONObject ( address ) ) ) ;
		else
			return pcn_dealSpecialProCityName ( pcn_getProCityNameURL ( json_object ) ) ;
		*/
		return pcn_dealSpecialProCityName ( pcn_getProCityNameURL ( json_object ) ) ;
	}
	
	/**
	 * 
	 * 处理省级、市级名称中特殊的情况。
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * 修改时间：2014年12月06日
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改内容：完善了省市名称的特例。
	 * 
	 * @param pro_city
	 * 
	 * @return
	 * 
	 */
	private static String[] pcn_dealSpecialProCityName(String[] pro_city)
	{
		//省级
		if(pro_city[0].equals ( "北京" ))
			pro_city[0] = "北京市" ;
		else if(pro_city[0].equals ( "天津" ))
			pro_city[0] = "天津市" ;
		else if(pro_city[0].equals ( "上海" ))
			pro_city[0] = "上海市" ;
		else if(pro_city[0].equals ( "重庆" ))
			pro_city[0] = "重庆市" ;
		else if(pro_city[0].equals ( "安徽" ))
			pro_city[0] = "安徽省" ;
		else if(pro_city[0].equals ( "福建" ))
			pro_city[0] = "福建省" ;
		else if(pro_city[0].equals ( "河北" ))
			pro_city[0] = "河北省" ;
		else if(pro_city[0].equals ( "河南" ))
			pro_city[0] = "河南省" ;
		else if(pro_city[0].equals ( "黑龙江" ))
			pro_city[0] = "黑龙江省" ;
		else if(pro_city[0].equals ( "湖北" ))
			pro_city[0] = "湖北省" ;
		else if(pro_city[0].equals ( "湖南" ))
			pro_city[0] = "湖南省" ;
		else if(pro_city[0].equals ( "辽宁" ))
			pro_city[0] = "辽宁省" ;
		else if(pro_city[0].equals ( "山东" ))
			pro_city[0] = "山东省" ;
		else if(pro_city[0].equals ( "江苏" ))
			pro_city[0] = "江苏省" ;
		else if(pro_city[0].equals ( "浙江" ))
			pro_city[0] = "浙江省" ;
		else if(pro_city[0].equals ( "江西" ))
			pro_city[0] = "江西省" ;
		else if(pro_city[0].equals ( "甘肃" ))
			pro_city[0] = "甘肃省" ;
		else if(pro_city[0].equals ( "山西" ))
			pro_city[0] = "山西省" ;
		else if(pro_city[0].equals ( "陕西" ))
			pro_city[0] = "陕西省" ;
		else if(pro_city[0].equals ( "吉林" ))
			pro_city[0] = "吉林省" ;
		else if(pro_city[0].equals ( "贵州" ))
			pro_city[0] = "贵州省" ;
		else if(pro_city[0].equals ( "云南" ))
			pro_city[0] = "云南省" ;
		else if(pro_city[0].equals ( "广东" ))
			pro_city[0] = "广东省" ;
		else if(pro_city[0].equals ( "四川" ))
			pro_city[0] = "四川省" ;
		else if(pro_city[0].equals ( "海南" ))
			pro_city[0] = "海南省" ;
		else if(pro_city[0].equals ( "青海" ))
			pro_city[0] = "青海省" ;		
		else if(pro_city[0].equals ( "台湾" ))
			pro_city[0] = "台湾省" ;
		
		//市级
		if(pro_city[1].equals ( "北京" ))
			pro_city[1] = "北京市" ;
		else if(pro_city[1].equals ( "天津" ))
			pro_city[1] = "天津市" ;
		else if(pro_city[1].equals ( "上海" ))
			pro_city[1] = "上海市" ;
		else if(pro_city[1].equals ( "重庆" ))
			pro_city[1] = "重庆市" ;
		else if(pro_city[1].equals ( "合肥" ))
			pro_city[1] = "合肥市" ;
		else if(pro_city[1].equals ( "广州" ))
			pro_city[1] = "广州市" ;
		else if(pro_city[1].equals ( "福州" ))
			pro_city[1] = "福州市" ;
		else if(pro_city[1].equals ( "厦门" ))
			pro_city[1] = "厦门市" ;
		else if(pro_city[1].equals ( "石家庄" ))
			pro_city[1] = "石家庄市" ;
		else if(pro_city[1].equals ( "郑州" ))
			pro_city[1] = "郑州市" ;
		else if(pro_city[1].equals ( "哈尔滨" ))
			pro_city[1] = "哈尔滨市" ;
		else if(pro_city[1].equals ( "武汉" ))
			pro_city[1] = "武汉市" ;
		else if(pro_city[1].equals ( "长沙" ))
			pro_city[1] = "长沙市" ;
		else if(pro_city[1].equals ( "南京" ))
			pro_city[1] = "南京市" ;
		else if(pro_city[1].equals ( "沈阳" ))
			pro_city[1] = "沈阳市" ;
		else if(pro_city[1].equals ( "济南" ))
			pro_city[1] = "济南市" ;
		else if(pro_city[1].equals ( "西安" ))
			pro_city[1] = "西安市" ;
		else if(pro_city[1].equals ( "杭州" ))
			pro_city[1] = "杭州市" ;
		else if(pro_city[1].equals ( "成都" ))
			pro_city[1] = "成都市" ;
		else if(pro_city[1].equals ( "昆明" ))
			pro_city[1] = "昆明市" ;
		else if(pro_city[1].equals ( "长春" ))
			pro_city[1] = "长春市" ;
		else if(pro_city[1].equals ( "兰州" ))
			pro_city[1] = "兰州市" ;
		else if(pro_city[1].equals ( "西宁" ))
			pro_city[1] = "西宁市" ;
		else if(pro_city[1].equals ( "太原" ))
			pro_city[1] = "太原市" ;
		else if(pro_city[1].equals ( "贵阳" ))
			pro_city[1] = "贵阳市" ;
		else if(pro_city[1].equals ( "南昌" ))
			pro_city[1] = "南昌市" ;
		else if(pro_city[1].equals ( "海口" ))
			pro_city[1] = "海口市" ;
		else if(pro_city[1].equals ( "台湾" ))
			pro_city[1] = "台湾市" ;

		if( pro_city[0].equals ( "香港特别行政区" ) )
			pro_city[1] = pro_city[0];
		else if( pro_city[0].equals ( "澳门特别行政区" ) )
			pro_city[1] = pro_city[0];
		
		return pro_city;
	}
	
	/**
	 * 
	 * 如果新浪微博数据里面含有省、市名称，那么就返回Address字段中的JSON对象的字符串
	 * 若无，则返回字符串false。
	 * 
	 * v1:	逐条校验，再解析，时间复杂度高: 157808条数据 283227ms
	 * 
	 * v2: 直接解析，报错就进异常，考虑到url_objects内存放的是数组，所以考虑数组长度为3（目前见过最长的，即使更长也很少很少，交由百度API处理）
	 * 		异常捕捉循环次数测试结果0次：157808条数据，导出680M，联网数据60.6M，
	 * 		异常捕捉循环次数测试结果1次：157808条数据，导出680M，联网数据32.6M，251554ms
	 * 		异常捕捉循环次数测试结果2次：157808条数据，导出680M，联网数据32.2M，256410ms, 
	 * 结论：多捕捉15万次异常，虽然省去80次左右访问百度API，但是联网开销比较大，最终采用2次异常捕捉循环
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param json_object
	 * 
	 * @return
	 * 
	 * @throws JSONException 
	 * 
	 */
	private static String pcn_haveProCityName( JSONObject json_object ) throws JSONException
	{
		/*
		JSONArray 	url_array 				= 		null ;
		JSONObject 	temp_json_object 	= 		null ;
		if(json_object.has ( "url_objects" ) && !json_object.isNull ( "url_objects" ))
		{
			url_array = json_object.getJSONArray("url_objects") ;
			for( int index = 0; index < url_array.length ( ); index++ )
			{
				if(!url_array.isNull ( index ))
				{
					temp_json_object	=	url_array.getJSONObject ( index ) ;
					if( temp_json_object.has ( "object" ) && !temp_json_object.getString ( "object" ).equals ( "" ) )
					{
						temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
						if( temp_json_object.has ( "object" ) && !temp_json_object.getString ( "object" ).equals ( "" ) && !temp_json_object.getString ( "object" ).equals ( "[]" ))
						{
							temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
							if( temp_json_object.has ( "address" ) && !temp_json_object.getString ( "address" ).equals ( "" ) )
							{
								temp_json_object	=	temp_json_object.getJSONObject ( "address" ) ;
								if( temp_json_object.has ( "region" ) && !temp_json_object.getString ( "region" ).equals ( "" ) )
								{
									if( temp_json_object.has ( "locality" ) && !temp_json_object.getString ( "locality" ).equals ( "" ) )
									{
										return	temp_json_object.toString ( ) ;
									}
								}
							}
						}
					}
				}
			}
		}
		//若之前没有数据返回，表示数据内没有省、市、名字的字段
		return "false";
		*/
		JSONArray 	url_array 				=		json_object.getJSONArray("url_objects") ;
		JSONObject 	temp_json_object 	= 		null ;
		try
		{
			temp_json_object	=	url_array.getJSONObject ( 0 ) ;
			temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
			temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
			temp_json_object	=	temp_json_object.getJSONObject ( "address" ) ;
			return	temp_json_object.toString ( ) ;
		}
		catch ( Exception e1 )
		{
			try
			{
				temp_json_object	=	url_array.getJSONObject ( 1 ) ;
				temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
				temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
				temp_json_object	=	temp_json_object.getJSONObject ( "address" ) ;
				return	temp_json_object.toString ( ) ;
			}
			catch ( Exception e2 )
			{
				try
				{
					temp_json_object	=	url_array.getJSONObject ( 1 ) ;
					temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
					temp_json_object	=	temp_json_object.getJSONObject ( "object" ) ;
					temp_json_object	=	temp_json_object.getJSONObject ( "address" ) ;
					return	temp_json_object.toString ( ) ;
				}
				catch ( Exception e3 )
				{
					return "false";
				}	
			}
		}
	}
	
	/**
	 * 
	 * 借助Baidu API通过经纬度得到地名
	 * 
	 * 添加时间：2014年11月29日
	 * 
	 * 添加人：李聪
	 * **************************************************************
	 * 修改时间：2014年12月2日
	 * 
	 * 修改人：李佳骏
	 *
	 * 修改内容：
	 * 		1.添加访问百度API返回结果为error的处理，防止程序崩
	 * 		  掉。若API返回为error,那么省、市设为未知。
	 * 		2.添加特殊字段处理。
	 * **************************************************************
	 * 修改时间：2014年12月3日
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改内容：
	 * 		添加快表技术，（带误差，可调整）
	 * **************************************************************
	 * **************************************************************
	 * 修改时间：2016年7月5日22:32:01
	 * 
	 * 修改人：肖濛
	 * 
	 * 修改内容：
	 * 		改进百度API为v2版本
	 * **************************************************************
	 * 
	 * @param jsonObject：一条微博信息
	 * 
	 * @return String[2] 省、市
	 * 
	 * @throws JSONException
	 * 
	 * @throws IOException 
	 * 
	 */
	static int qt_conut = 0;
	private static String[] pcn_getProCityNameURL(JSONObject jsonObject) throws  IOException
	{
		String[]			cityName			=		null;
		try
		{
			double 				location[] 			= 		pcn_getCoordinates( jsonObject ) ;	
			//快表搜索
			cityName = QuickTable.search ( location[0], location[1] ) ;
			if( cityName != null )
			{
				qt_conut++;
				System.out.println ( qt_conut+"快表"+location[0]+"\t"+location[1] + ":\t"+ cityName[0]+"-"+cityName[1] + "-" + QuickTable.getLength ( ));
				return cityName ;
			}
			
			//URL搜索
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
			
			//入快表
			QuickTable.put ( cityName[0], cityName[1], location[0], location[1] );
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
	 * 
	 * 获取新浪微博数据里面的坐标信息
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param json_object
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * 
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
	
	/**
	 * 
	 * 从地址JSON对象中直接提取地名
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param jsonObject
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * 
	 */
	private static String[] pcn_getAddressObjectProCityName(JSONObject address_object) throws JSONException
	{
		String[] pro_city_name 	= 		new String[2] ;
		pro_city_name[0] 			= 		address_object.getString("region") ;
		pro_city_name[1] 			= 		address_object.getString("locality") ;
		return 	pro_city_name;
	}
	
	/**
	 * 
	 * 针对按行返回的数据，通过URL来进行读取的函数。
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param url
	 * 
	 * @return String
	 * 
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
	 * 
	 * 将时间格式解析成 year-month-day 
	 * 如：输入Sat Nov 15 18:41:39 +0800 2014，会解析成2014-11.01
	 * 
	 * 添加时间：2014年12月02日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param str
	 * 
	 * @return
	 * 
	 * @throws ParseException
	 * 
	 */
	public static String date_parse(String date) throws ParseException 
	{
		if( date == null ) 
		{
			return null ;
		}
		else
		{
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");    
			return format.format ( new Date(date) );
		}
    }
}
