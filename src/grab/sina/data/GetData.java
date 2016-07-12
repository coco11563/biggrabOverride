
package grab.sina.data;


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import stor.OperMongo;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import grab.sina.config.readConfig;
import grab.sina.data.statistics.Statistics;

/**
 * 
 * 提供获取Sina数据的类
 * 
 * 项目名称: BigDataGrab
 * 
 * 包: grab.sina.data
 * 
 * 类名称: GetData
 * 
 * 类描述: 提供添加半径参数的获取Sina数据的类
 * 
 * 创建人: coco1
 * 
 * 创建时间: 2016年6月1日17:44:20
 * 
 * 
 */
public class GetData
{
	public static void getSinaData_new_test(String collection_name, double lat_min, double lon_min, double lat_max,
			double lon_max, long unix_start_time, long unix_end_time,int range) throws JSONException, IOException, ParseException,
					SQLException, NoSuchFieldException, SecurityException, InterruptedException

	{
		String[] access_token = readConfig.read_access_token_config();
		int access_token_total = access_token.length;
		int access_token_current = 0; // 当前钥匙编码
		int sim_whole = 0; // 累计总数据
		int sim_error = 0; // 累计取错
		int sim_blank = 0; // 累计取空
		int blank_back = 0; // 一次URL取数据累计取空次数
		int error_back = 0; // 一次URL取数据累计取空次数
		int area_range = range; // 数据辐射半径
		int count = 50; // 每页数据量
		int pages = 1; // 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int data_total_number = 0; // 记录地区总的微博量
		double latlon = (double)range/111320;// 11132M＝0.1度
		for (int i = 0; i < 2; i++) {
			if (i == 1) {
				lat_min = lat_min + latlon;
				lon_min = lon_min + latlon;
				lat_max = lat_max - latlon;
				lon_max = lon_max - latlon;
			}
			for (double lat = lat_min; lat < lat_max - latlon; lat = lat + 2 * latlon) {
				for (double lon = lon_min; lon < lon_max - latlon; lon = lon + 2 * latlon) {
					blank_back = 0;
					error_back = 0;
					for (int page = 1; page <= pages; page++) {
						// 计算使用的账户
						access_token_current = access_token_current % (access_token_total);
						String URL = ProcessData.url_nearby_timeline(access_token[access_token_current], lat, lon,
								unix_end_time, unix_start_time, area_range, count, page);
						Thread.sleep(100);// 降低访问频率的关键点一：休眠
						System.out.println("\r\n\r\n该区域第：" + sim_whole + "次抓取。");
						System.out.println("开始获取：" + URL);
						System.out.println("抓取日期：" + collection_name);
						System.out.println("纬度：" + lat + "度。");
						System.out.println("经度：" + lon + "度。");
						String json_data = ProcessData.connUrl(URL);
						sim_whole++;

						// 情况一：取空：有时候再取几次就能取到数据
						if (json_data.equals("[]")) {
							if (2 > blank_back) // 重复取空3次，就舍弃，认为是不存在数据（基本是不存在数据，实际未证实）
							{
								blank_back++;
								sim_blank++;
								page--;

								System.out.println("-----------取空->重取-------------\r\n\r\n");
								continue;
							} else {
								blank_back = 0;
								sim_blank++;

								System.out.println("-----------取空->舍弃-------------\r\n\r\n");
								break;
							}
						}
						// 情况二：取错：ERROR，说明该账户请求次数超出限制。
						else if (json_data == "error") {
							sim_error++;
							error_back++;
							page--;
							access_token_current++;

							if (error_back < access_token_total * 2) // 如果刷过两轮所有的access_token都取错，证明次数不够了，休眠到下一个整点才开始继续取数据。
							{
								System.out.println("-----------取错->换Key-------------\r\n\r\n");
							} else {
								long time = ProcessData.getSecondsToNextClockSharp();
								System.out.println("---------取错->休眠至整点:" + time + "s----------\r\n\r\n");
								Thread.sleep(1000 * time);
								error_back = 0;
							}
							continue;
						}
						// 情况三：正常
						else {
							// 解析json数据

							try {
								JSONObject js = new JSONObject(json_data);
								if (!js.getString("total_number").equals(""))
									data_total_number = Integer.parseInt(js.getString("total_number"));
								else
									break;

								// 下一次页面处理计算
								pages = data_total_number / 50 + 1;

								// 建立数据数组
								JSONArray array = new JSONArray();
								if (false == js.isNull("statuses"))
									array = js.getJSONArray("statuses");
								else
									break;

								// 插入到数据库中数据
								for (int j = 0; j < array.length(); j++) {
									String geo = array.getJSONObject(j).getString("geo");
									if (geo.equals("null")) {
										continue;
									}
									JSONObject geoObject = new JSONObject(geo);
									if (geoObject.getString("coordinates").equals("null")) {
										continue;
									}
									String final_data = array.getJSONObject(j).toString();
									if (final_data.contains("com.weico.topicinfo")) {
										continue;
									}

									double location[] = ProcessData.getGeo(array.getJSONObject(j));
									// web墨卡托作为DB名称，时间作为Collection名称
									OperMongo.saveData(final_data, ProcessData.wkt_code(location[1], location[0], 5),
											ProcessData.wkt_code(location[1], location[0], 9) + "_" + collection_name);
								}
								System.out.println("<-----------入库------------->");
								access_token_current++;// 降低访问频率的关键点一：提高access_token切换次数。
							} catch (JSONException e) {
								System.out.println(e.getMessage());
							}
							// 获取总数据

						}
					}
				}
			}
		}
		Statistics.statisticsWrite(sim_whole, sim_error, sim_blank, 0, 0, 0);
		System.gc();
	}
	/**
	 * 
	 * 测试版：该函数目的是为了测试，对同一个access_token降低使用频率后的抓取效果有没有提升
	 * 
	 * 添加时间：2015年04月13日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param collection_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @throws SQLException
	 * 
	 * @throws ParseException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 * @throws InterruptedException 
	 * 
	 */
	public static double getSinaData_new_test ( 	String 							collection_name,
																				double 							lat_min,
																				double 							lon_min, 
																				double 							lat_max, 
																				double 							lon_max,
																				long 							unix_start_time, 
																				long 							unix_end_time ) 
																				throws 
																				JSONException,
																				IOException, 
																				ParseException, 
																				SQLException, 
																				NoSuchFieldException,
																				SecurityException, InterruptedException

	{
		String[] access_token 			= readConfig.read_access_token_config ( );
		int 	access_token_total 		= access_token.length;
		int 	access_token_current 	= 0;			// 当前钥匙编码
		int 	sim_whole 				= 0; 			// 累计总数据
		int 	sim_error 				= 0; 			// 累计取错
		int 	sim_blank 				= 0; 			// 累计取空
		int		blank_back				= 0; 			// 一次URL取数据累计取空次数
		int		error_back				= 0;			// 一次URL取数据累计取空次数
		int 	area_range 				= 11132;	// 数据辐射半径
		int 	count 					= 50;			// 每页数据量
		int 	pages 					= 1;			// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int 	data_total_number 		= 0;			// 记录地区总的微博量
		double 	voidGetNum					= 0;			//统计空抓后抓到次数
		double	latlon					= 0.1;//11132M＝0.1度
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min 	= lat_min 	+ latlon;
				lon_min 	= lon_min 	+ latlon;
				lat_max 	= lat_max	- latlon;
				lon_max 	= lon_max 	- latlon;
			}
			for ( double lat = lat_min; lat < lat_max - latlon; lat = lat + 2*latlon )
			{
				for ( double lon = lon_min; lon < lon_max - latlon; lon = lon + 2*latlon )
				{
					blank_back = 0;
					error_back = 0;
					for ( int page = 1; page <= pages; page++ )
					{
						// 计算使用的账户
						access_token_current = access_token_current% ( access_token_total );
						String URL = ProcessData.url_nearby_timeline (access_token [ access_token_current ], lat,lon, unix_end_time, unix_start_time,area_range, count, page );
						Thread.sleep(100);//降低访问频率的关键点一：休眠
						System.out.println ( "\r\n\r\n该区域第：" + sim_whole+ "次抓取。" );
						System.out.println ( "开始获取：" + URL );
						System.out.println ( "抓取日期：" + collection_name );
						System.out.println ( "纬度：" + lat + "度。" );
						System.out.println ( "经度：" + lon + "度。" );
						String json_data = ProcessData.connUrl ( URL );
						sim_whole++;
						
						// 情况一：取空：有时候再取几次就能取到数据
						// 添加抓空后抓取成功次数统计
						if ( json_data.equals ( "[]" ) )
						{
							if(2 > blank_back)//重复取空3次，就舍弃，认为是不存在数据（基本是不存在数据，实际未证实）
							{
								blank_back++;
								sim_blank++;
								page--;
								
								System.out.println ( "-----------取空->重取-------------\r\n\r\n" );
								continue;
							}
							else
							{
								blank_back = 0;
								sim_blank++;
								
								System.out.println ( "-----------取空->舍弃-------------\r\n\r\n" );
								break;
							}
						}
						// 情况二：取错：ERROR，说明该账户请求次数超出限制。
						else if ( json_data == "error" )
						{
							sim_error++;
							error_back++;
							page--;
							access_token_current++;
							
							if(error_back<access_token_total * 2)//如果刷过两轮所有的access_token都取错，证明次数不够了，休眠到下一个整点才开始继续取数据。
							{
								System.out.println ( "-----------取错->换Key-------------\r\n\r\n" );
							}
							else
							{
								long time = ProcessData.getSecondsToNextClockSharp();
								System.out.println ( "---------取错->休眠至整点:"+time+"s----------\r\n\r\n" );
								Thread.sleep(1000*time);
								error_back = 0;
							}
							continue;
						}
						// 情况三：正常
						else
						{
							if(blank_back != 0)//当这次抓取之前有空抓
							{
								voidGetNum ++ ;
							}
							// 解析json数据
							
							try{
							JSONObject js = new JSONObject ( json_data );
							if ( !js.getString ( "total_number" ).equals ( "" ) )
								data_total_number = Integer.parseInt ( js.getString ( "total_number" ) );
							else
								break;
														
							//下一次页面处理计算
							pages = data_total_number / 50 + 1;			
							
							// 建立数据数组
							JSONArray array = new JSONArray ( );
							if ( false == js.isNull ( "statuses" ) )
								array = js.getJSONArray ( "statuses" );
							else
								break;
							
							// 插入到数据库中数据
							for ( int j = 0; j < array.length ( ); j++ )
							{
								String geo = array.getJSONObject ( j ).getString ( "geo" );
								if ( geo.equals ( "null" ) )
								{
									continue;
								}
								JSONObject geoObject = new JSONObject ( geo );
								if ( geoObject.getString ( "coordinates" ).equals ( "null" ) )
								{
									continue;
								}
								String final_data = array.getJSONObject ( j ).toString ( );
								if(final_data.contains("com.weico.topicinfo")) 
								{
									continue;
								}
								
								double location[] = ProcessData.getGeo ( array.getJSONObject ( j ) );
								//web墨卡托作为DB名称，时间作为Collection名称
								OperMongo.saveData ( final_data, ProcessData.wkt_code (location [ 1 ], location [ 0 ], 5 ), ProcessData.wkt_code (location [ 1 ], location [ 0 ], 9 )+"_"+collection_name );
							}
							System.out.println ( "<-----------入库------------->" );
							access_token_current++;//降低访问频率的关键点一：提高access_token切换次数。
							}
							catch(JSONException e)
							{
								System.out.println(e.getMessage());
							}
							// 获取总数据
							
						}
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
		double rate = voidGetNum;
		return rate;
	}
	
	/**
	 * 
	 * 优化版：定制出来的函数，其目的是实现9级wkt块号做数据库名称， 5级wkt块号+时间序列做数据集名称
	 * 
	 * 添加时间：2014年11月23日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param collection_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @throws SQLException
	 * 
	 * @throws ParseException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * @throws InterruptedException 
	 * 
	 */
	public static void getSinaData_wkt_wkt_time_lite ( 	String 							collection_name,
																				double 							lat_min,
																				double 							lon_min, 
																				double 							lat_max, 
																				double 							lon_max,
																				long 							unix_start_time, 
																				long 							unix_end_time ) 
																				throws 
																				JSONException,
																				IOException, 
																				ParseException, 
																				SQLException, 
																				NoSuchFieldException,
																				SecurityException, InterruptedException

	{
		String [ ] access_token 		= readConfig.read_access_token_config ( );
		int 	access_token_total 		= access_token.length;
		int 	access_token_current 	= 0;			// 当前钥匙编码
		int 	sim_whole 					= 0; 			// 累计总数据
		int 	sim_error 					= 0; 			// 累计取错
		int 	sim_blank 					= 0; 			// 累计取空
		int	blank_back				= 0; 			// 一次URL取数据累计取空次数
		int	error_back					= 0;			// 一次URL取数据累计取空次数
		int 	area_range 				= 11132;	// 数据辐射半径
		int 	count 						= 50;			// 每页数据量
		int 	pages 						= 1;			// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int 	data_total_number 		= 0;			// 记录地区总的微博量
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min 		= lat_min 		+ 0.1;
				lon_min 	= lon_min 		+ 0.1;
				lat_max 		= lat_max		- 0.1;
				lon_max 	= lon_max 	- 0.1;
			}
			for ( double lat = lat_min; lat < lat_max - 0.1; lat = lat + 0.2 )
			{
				for ( double lon = lon_min; lon < lon_max - 0.1; lon = lon + 0.2 )
				{
					blank_back = 0;
					error_back = 0;
					for ( int page = 1; page <= pages; page++ )
					{
						// 计算使用的账户
						access_token_current = access_token_current% ( access_token_total );
						String URL = ProcessData.url_nearby_timeline (access_token [ access_token_current ], lat,lon, unix_end_time, unix_start_time,area_range, count, page );
						System.out.println ( "\r\n\r\n该区域第：" + sim_whole+ "次抓取。" );
						System.out.println ( "开始获取：" + URL );
						System.out.println ( "抓取日期：" + collection_name );
						System.out.println ( "纬度：" + lat + "度。" );
						System.out.println ( "经度：" + lon + "度。" );
						String json_data = ProcessData.connUrl ( URL );
						sim_whole++;
						
						// 情况一：取空：有时候再取几次就能取到数据
						if ( json_data.equals ( "[]" ) )
						{
							if(2 > blank_back)//重复取空3次，就舍弃
							{
								blank_back++;
								sim_blank++;
								page--;
								
								System.out.println ( "-----------取空->重取-------------\r\n\r\n" );
								continue;
							}
							else
							{
								blank_back = 0;
								sim_blank++;
								
								System.out.println ( "-----------取空->舍弃-------------\r\n\r\n" );
								break;
							}
						}
						// 情况二：取错：ERROR，说明该账户请求次数超出限制。
						else if ( json_data == "error" )
						{
							sim_error++;
							error_back++;
							page--;
							access_token_current++;
							
							if(error_back<access_token_total * 2)//如果刷过两轮所有的access_token都取错，证明次数不够了，休眠到下一个整点才开始继续取数据。
							{
								System.out.println ( "-----------取错->换Key-------------\r\n\r\n" );
							}
							else
							{
								long time = ProcessData.getSecondsToNextClockSharp();
								System.out.println ( "---------取错->休眠至整点:"+time+"s----------\r\n\r\n" );
								Thread.sleep(1000*time);
								error_back = 0;
							}
							continue;
						}
						// 情况三：正常
						else
						{
							// 解析json数据
							JSONObject js = new JSONObject ( json_data );
							
							// 获取总数据
							if ( !js.getString ( "total_number" ).equals ( "" ) )
								data_total_number = Integer.parseInt ( js.getString ( "total_number" ) );
							else
								break;
														
							//下一次页面处理计算
							pages = data_total_number / 50 + 1;			
							
							// 建立数据数组
							JSONArray array = new JSONArray ( );
							if ( false == js.isNull ( "statuses" ) )
								array = js.getJSONArray ( "statuses" );
							else
								break;
							
							// 插入到数据库中数据
							for ( int j = 0; j < array.length ( ); j++ )
							{
								String geo = array.getJSONObject ( j ).getString ( "geo" );
								if ( geo.equals ( "null" ) )
								{
									continue;
								}
								JSONObject geoObject = new JSONObject ( geo );
								if ( geoObject.getString ( "coordinates" ).equals ( "null" ) )
								{
									continue;
								}
								String final_data = array.getJSONObject ( j ).toString ( );
								if(final_data.contains("com.weico.topicinfo")) 
								{
									continue;
								}
								
								double location[] = ProcessData.getGeo ( array.getJSONObject ( j ) );
								//web墨卡托作为DB名称，时间作为Collection名称
								OperMongo.saveData ( final_data, ProcessData.wkt_code (location [ 1 ], location [ 0 ], 5 ), ProcessData.wkt_code (location [ 1 ], location [ 0 ], 9 )+"_"+collection_name );
							}
							System.out.println ( "<-----------入库------------->" );
						}
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
	}
	
	/**
	 * 
	 * 优化版：定制出来的函数，其目的是实现时间序列做数据库名称， wkt块号做数据集名称
	 * 
	 * 添加时间：2014年11月22日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param db_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @throws SQLException
	 * 
	 * @throws ParseException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 */
	public static void getSinaData_time_wkt_lite ( 	String 							db_name,
																		double 							lat_min,
																		double 							lon_min, 
																		double 							lat_max, 
																		double 							lon_max,
																		long 							unix_start_time, 
																		long 							unix_end_time ) 
																		throws 
																		JSONException,
																		IOException, 
																		ParseException, 
																		SQLException, 
																		NoSuchFieldException,
																		SecurityException

	{
		String [ ] access_token = readConfig.read_access_token_config ( );
		int 	access_token_total = access_token.length;
		int 	access_token_current 	= 0;			// 当前钥匙编码
		int 	sim_whole 					= 0; 			// 累计总数据
		int 	sim_error 					= 0; 			// 累计取错
		int 	sim_blank 					= 0; 			// 累计取空
		int	blank_back				= 0; 			// 一次个URL累计取空次数
		int 	area_range 				= 11132;	// 数据辐射半径
		int 	count 						= 50;			// 每页数据量
		int 	pages 						= 1;			// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int 	data_total_number 		= 0;			// 记录地区总的微博量
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min 		= lat_min 		+ 0.1;
				lon_min 	= lon_min 		+ 0.1;
				lat_max 		= lat_max		- 0.1;
				lon_max 	= lon_max 	- 0.1;
			}
			for ( double lat = lat_min; lat < lat_max - 0.1; lat = lat + 0.2 )
			{
				for ( double lon = lon_min; lon < lon_max - 0.1; lon = lon + 0.2 )
				{
					blank_back = 0;
					for ( int page = 1; page <= pages; page++ )
					{
						// 计算使用的账户
						access_token_current = access_token_current% ( access_token_total );
						String URL = ProcessData.url_nearby_timeline (access_token [ access_token_current ], lat,lon, unix_end_time, unix_start_time,area_range, count, page );
						System.out.println ( "\r\n\r\n该区域第：" + sim_whole+ "次抓取。" );
						System.out.println ( "开始获取：" + URL );
						System.out.println ( "纬度：" + lat + "度。" );
						System.out.println ( "经度：" + lon + "度。" );
						String json_data = ProcessData.connUrl ( URL );
						sim_whole++;
						
						// 情况一：取空：有时候再取几次就能取到数据
						if ( json_data.equals ( "[]" ) )
						{
							if(2 > blank_back)//重复取空3次，就舍弃
							{
								blank_back++;
								sim_blank++;
								page--;
								
								System.out.println ( "-----------取空->重取-------------\r\n\r\n" );
								continue;
							}
							else
							{
								blank_back = 0;
								sim_blank++;
								
								System.out.println ( "-----------取空->舍弃-------------\r\n\r\n" );
								break;
							}
						}
						// 情况二：取错：ERROR，说明该账户请求次数超出限制。
						else if ( json_data == "error" )
						{
							sim_error++;
							page--;
							access_token_current++;
							
							System.out.println ( "-----------取错->舍弃-------------\r\n\r\n" );
							continue;
						}
						// 情况三：正常
						else
						{
							// 解析json数据
							JSONObject js = new JSONObject ( json_data );
							
							// 获取总数据
							if ( !js.getString ( "total_number" ).equals ( "" ) )
								data_total_number = Integer.parseInt ( js.getString ( "total_number" ) );
							else
								break;
														
							//下一次页面处理计算
							pages = data_total_number / 50 + 1;			
							
							// 建立数据数组
							JSONArray array = new JSONArray ( );
							if ( false == js.isNull ( "statuses" ) )
								array = js.getJSONArray ( "statuses" );
							else
								break;
							
							// 插入到数据库中数据
							for ( int j = 0; j < array.length ( ); j++ )
							{
								String geo = array.getJSONObject ( j ).getString ( "geo" );
								if ( geo.equals ( "null" ) )
								{
									continue;
								}
								JSONObject geoObject = new JSONObject ( geo );
								if ( geoObject.getString ( "coordinates" ).equals ( "null" ) )
								{
									continue;
								}
								double location[] = ProcessData.getGeo ( array.getJSONObject ( j ) );
								//web墨卡托作为DB名称，时间作为Collection名称
								OperMongo.saveData ( 	 array.getJSONObject ( j ).toString ( ), db_name, ProcessData.wkt_code (location [ 1 ], location [ 0 ], 9 ) );
							}
							System.out.println ( "<-----------入库------------->" );
						}
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
	}
	
	/**
	 * 
	 * 优化版：定制出来的函数，其目的是实现wkt块号做数据库名称， 时间序列做数据集名称
	 * 
	 * 添加时间：2014年11月21日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param collection_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @throws SQLException
	 * 
	 * @throws ParseException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 */
	public static void getSinaData_wkt_time_lite ( 	String 							collection_name,
																		double 							lat_min,
																		double 							lon_min, 
																		double 							lat_max, 
																		double 							lon_max,
																		long 							unix_start_time, 
																		long 							unix_end_time ) 
																		throws 
																		JSONException,
																		IOException, 
																		ParseException, 
																		SQLException, 
																		NoSuchFieldException,
																		SecurityException

	{
		String [ ] access_token = readConfig.read_access_token_config ( );
		int 	access_token_total = access_token.length;
		int 	access_token_current 	= 0;			// 当前钥匙编码
		int 	sim_whole 					= 0; 			// 累计总数据
		int 	sim_error 					= 0; 			// 累计取错
		int 	sim_blank 					= 0; 			// 累计取空
		int	blank_back				= 0; 			// 一次个URL累计取空次数
		int 	area_range 				= 11132;	// 数据辐射半径
		int 	count 						= 50;			// 每页数据量
		int 	pages 						= 1;			// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int 	data_total_number 		= 0;			// 记录地区总的微博量
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min 		= lat_min 		+ 0.1;
				lon_min 	= lon_min 		+ 0.1;
				lat_max 		= lat_max		- 0.1;
				lon_max 	= lon_max 	- 0.1;
			}
			for ( double lat = lat_min; lat < lat_max - 0.1; lat = lat + 0.2 )
			{
				for ( double lon = lon_min; lon < lon_max - 0.1; lon = lon + 0.2 )
				{
					blank_back = 0;
					for ( int page = 1; page <= pages; page++ )
					{
						// 计算使用的账户
						access_token_current = access_token_current% ( access_token_total );
						String URL = ProcessData.url_nearby_timeline (access_token [ access_token_current ], lat,lon, unix_end_time, unix_start_time,area_range, count, page );
						System.out.println ( "\r\n\r\n该区域第：" + sim_whole+ "次抓取。" );
						System.out.println ( "开始获取：" + URL );
						System.out.println ( "纬度：" + lat + "度。" );
						System.out.println ( "经度：" + lon + "度。" );
						String json_data = ProcessData.connUrl ( URL );
						sim_whole++;
						
						// 情况一：取空：有时候再取几次就能取到数据
						if ( json_data.equals ( "[]" ) )
						{
							if(2 > blank_back)//重复取空3次，就舍弃
							{
								blank_back++;
								sim_blank++;
								page--;
								
								System.out.println ( "-----------取空->重取-------------\r\n\r\n" );
								continue;
							}
							else
							{
								blank_back = 0;
								sim_blank++;
								
								System.out.println ( "-----------取空->舍弃-------------\r\n\r\n" );
								break;
							}
						}
						// 情况二：取错：ERROR，说明该账户请求次数超出限制。
						else if ( json_data == "error" )
						{
							sim_error++;
							page--;
							access_token_current++;
							
							System.out.println ( "-----------取错->换Key-------------\r\n\r\n" );
							continue;
						}
						// 情况三：正常
						else
						{
							// 解析json数据
							JSONObject js = new JSONObject ( json_data );
							
							// 获取总数据
							if ( !js.getString ( "total_number" ).equals ( "" ) )
								data_total_number = Integer.parseInt ( js.getString ( "total_number" ) );
							else
								break;
														
							//下一次页面处理计算
							pages = data_total_number / 50 + 1;			
							
							// 建立数据数组
							JSONArray array = new JSONArray ( );
							if ( false == js.isNull ( "statuses" ) )
								array = js.getJSONArray ( "statuses" );
							else
								break;
							
							// 插入到数据库中数据
							for ( int j = 0; j < array.length ( ); j++ )
							{
								String geo = array.getJSONObject ( j ).getString ( "geo" );
								if ( geo.equals ( "null" ) )
								{
									continue;
								}
								JSONObject geoObject = new JSONObject ( geo );
								if ( geoObject.getString ( "coordinates" ).equals ( "null" ) )
								{
									continue;
								}
								double location[] = ProcessData.getGeo ( array.getJSONObject ( j ) );
								//web墨卡托作为DB名称，时间作为Collection名称
								OperMongo.saveData ( 	 array.getJSONObject ( j ).toString ( ), ProcessData.wkt_code (location [ 1 ], location [ 0 ], 9 ),collection_name );
							}
							System.out.println ( "<-----------入库------------->" );
						}
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
	}

	/**
	 * 
	 * 注：此抓取数据的函数是最早的版本，目前已经不用
	 * 
	 * 其目的是实现wkt块号做数据库名称， 时间序列做数据集名称
	 * 
	 * @param collection_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @throws SQLException
	 * 
	 * @throws ParseException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 */
	public static void getSinaData_wkt_time ( 	String 							collection_name,
																	double 							lat_min,
																	double 							lon_min, 
																	double 							lat_max, 
																	double 							lon_max,
																	long 							unix_start_time, 
																	long 							unix_end_time ) 
																	throws 
																	JSONException,
																	IOException, 
																	ParseException, 
																	SQLException, 
																	NoSuchFieldException,
																	SecurityException
	{
		int sim_whole = 0; // 累计总数据
		int sim_error = 0; // 累计取错
		int sim_blank = 0; // 累计取空
		int area_range = 11132;
		int count = 50;
		String [ ] access_token = readConfig.read_access_token_config ( );
		int access_token_total = access_token.length;
		int access_token_current = 0;
		int pages = 1;// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int data_total_number = 0;// 记录地区总的微博量
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min = lat_min + 0.1;
				lon_min = lon_min + 0.1;
				lat_max = lat_max - 0.1;
				lon_max = lon_max - 0.1;
			}
			for ( double lat = lat_min; lat < lat_max - 0.1; lat = lat + 0.2 )
			{
				for ( double lon = lon_min; lon < lon_max - 0.1; lon = lon + 0.2 )
				{
					for ( int page = 1; page <= pages; page++ )
					{
						SimpleDateFormat df = new SimpleDateFormat (
								"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
						System.out.println ( "开始    current time:"
								+ df.format ( new Date ( ) ) );
						// 计算使用的账户
						access_token_current = access_token_current
								% ( access_token_total );
						String URL = "https://api.weibo.com/2/place/nearby_timeline.json?"
								+ "access_token="
								+ access_token [ access_token_current ]
								+ "&lat="
								+ lat
								+ "&long="
								+ lon
								+ "&endtime="
								+ unix_end_time
								+ "&starttime="
								+ unix_start_time
								+ "&range="
								+ area_range
								+ "&count=" + count + "&page=" + page;
						String json_data = "[]";// 取空则反复求取
						int blank_count = 0; // 取空计数，超过一定次数记录其URL
						while ( json_data.equals ( "[]" ) )
						{
							if ( json_data.equals ( "[]" ) )
							{
								sim_blank++;
								blank_count++;
								if ( blank_count == 5 )
								{
									break;
								}
							}
							json_data = ProcessData.connUrl ( URL );
							sim_whole++;
							System.out.println ( URL );
						}
						if ( blank_count == 5 )
						{
							System.out
									.println ( "-----------取空-------------\r\n\r\n" );
							break;
						}
						if ( json_data == "error" )
						{
							System.out
									.println ( "-----------取错-------------\r\n\r\n" );
							sim_error++;
							access_token_current++;// 切换账户,通常是HTTP403
													// ERROR，说明该账户请求次数超出限制。
							System.out.println ( "error" );
							page--;// 页码回退,使得该点能被重新请求
							continue;
						}
						// 解析json数据
						JSONObject js = new JSONObject ( json_data );
						// 对下一页数据进行处理
						if ( js.getString ( "total_number" ) == "" )
						{
							break;
						}
						data_total_number = Integer.parseInt ( js
								.getString ( "total_number" ) );
						pages = data_total_number / 50 + 1;
						if ( data_total_number - ( page + 1 ) * 50 >= 0
								|| data_total_number % 50 == 0
								|| data_total_number <= 50 || pages == page )
							count = 50;
						else
							count = data_total_number - ( page ) * 50;
						// 建立数据数组
						JSONArray array = new JSONArray ( );
						if ( js.isNull ( "statuses" ) == false )// 建立数据数组
							array = js.getJSONArray ( "statuses" );
						else
							break;
						System.out.println ( "将json数据添加到导入队列中" );
						// 插入到数据库中数据
						DBObject data = null;
						for ( int j = 0; j < array.length ( ); j++ )
						{
							String geo = array.getJSONObject ( j ).getString (
									"geo" );
							if ( geo.equals ( "null" ) )
							{
								System.out.println ( "经纬度为空" );
								continue;
							}
							JSONObject geoObject = new JSONObject ( geo );
							if ( geoObject.getString ( "coordinates" ).equals (
									"null" ) )
							{
								System.out.println ( "经纬度为空" );
								continue;
							}
							double location[] = ProcessData.getGeo ( array
									.getJSONObject ( j ) );
							String db_name = ProcessData.wkt_code (
									location [ 1 ], location [ 0 ], 9 );
							JSONObject json_array_data = array
									.getJSONObject ( j );
							data = (DBObject) com.mongodb.util.JSON
									.parse ( json_array_data.toString ( ) );
							OperMongo
									.saveData ( data, db_name, collection_name );
						}
						System.out.println ( "<-----------入库------------->" );
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
	}

	/**
	 * 
	 * 注：此抓取数据的函数是最早的版本，目前已经不用
	 * 
	 * 定制出来的函数，其目的是实现时间序列做数据库名称， wkt块号做数据集名称
	 * 
	 * @param table_name
	 * 
	 * @param lat_min
	 * 
	 * @param lon_min
	 * 
	 * @param lat_max
	 * 
	 * @param lon_max
	 * 
	 * @param unix_start_time
	 * 
	 * @param unix_end_time
	 * 
	 * @param area_range
	 * 
	 * @param count
	 * 
	 * @throws JSONException
	 * 
	 * @throws IOException
	 * 
	 * @throws ParseException
	 * 
	 * @throws SQLException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 */
	public static void getSinaData_time_wkt ( 	String 							table_name,
																	double 							lat_min,
																	double 							lon_min, 
																	double 							lat_max, 
																	double 							lon_max,
																	long 							unix_start_time, 
																	long 							unix_end_time ) 
																	throws 
																	JSONException,
																	IOException, 
																	ParseException, 
																	SQLException, 
																	NoSuchFieldException,
																	SecurityException
	{
		int sim_whole = 0; // 累计总数据
		int sim_error = 0; // 累计取错
		int sim_blank = 0; // 累计取空
		// 配置变量
		int area_range = 11132;
		int count = 50;
		String [ ] access_token = readConfig.read_access_token_config ( );
		int access_token_total = access_token.length;
		int access_token_current = 0;
		int pages = 1;// 取大于一的数，为了处理最后一页URL。取大了并不会影响，下面会求出精确的数值。
		int data_total_number = 0;// 记录地区总的微博量
		for ( int i = 0; i < 2; i++ )
		{
			if ( i == 1 )
			{
				lat_min = lat_min + 0.1;
				lon_min = lon_min + 0.1;
				lat_max = lat_max - 0.1;
				lon_max = lon_max - 0.1;
			}
			for ( double lat = lat_min; lat < lat_max - 0.1; lat = lat + 0.2 )
			{
				for ( double lon = lon_min; lon < lon_max - 0.1; lon = lon + 0.2 )
				{
					for ( int page = 1; page <= pages; page++ )
					{
						SimpleDateFormat df = new SimpleDateFormat (
								"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
						System.out.println ( "开始    current time:"
								+ df.format ( new Date ( ) ) );
						// 计算使用的账户
						access_token_current = access_token_current
								% ( access_token_total );
						String URL = ProcessData.url_nearby_timeline (
								access_token [ access_token_current ], lat,
								lon, unix_end_time, unix_start_time,
								area_range, count, page );
						String json_data = "[]";// 取空则反复求取
						int blank_count = 0; // 取空计数，超过一定次数记录其URL
						while ( json_data.equals ( "[]" ) )
						{
							if ( json_data.equals ( "[]" ) )
							{
								sim_blank++;
								blank_count++;
								if ( blank_count == 5 )
								{
									break;
								}
							}
							json_data = ProcessData.connUrl ( URL );
							sim_whole++;
							System.out.println ( "获取JSON数据" );
							System.out.println ( URL );
						}
						if ( blank_count == 5 )
						{
							System.out.println ( "取空" );
							System.out.println ( URL );
							break;
						}
						if ( json_data == "error" )
						{
							sim_error++;
							access_token_current++;// 切换账户,通常是HTTP403
													// ERROR，说明该账户请求次数超出限制。
							System.out.println ( "error" );
							page--;// 页码回退,使得该点能被重新请求
							continue;
						}
						// 解析json数据
						JSONObject js = new JSONObject ( json_data );
						// 对下一页数据进行处理
						if ( js.getString ( "total_number" ) == "" )
						{
							break;
						}
						data_total_number = Integer.parseInt ( js
								.getString ( "total_number" ) );
						pages = data_total_number / 50 + 1;
						if ( data_total_number - ( page + 1 ) * 50 >= 0
								|| data_total_number % 50 == 0
								|| data_total_number <= 50 || pages == page )
							count = 50;
						else
							count = data_total_number - ( page ) * 50;
						// 建立数据数组
						JSONArray array = new JSONArray ( );
						if ( js.isNull ( "statuses" ) == false )// 建立数据数组
							array = js.getJSONArray ( "statuses" );
						else
							break;
						System.out.println ( "将json数据添加到导入队列中" );
						// 插入到数据库中数据
						DBObject data = null;
						for ( int j = 0; j < array.length ( ); j++ )
						{
							String geo = array.getJSONObject ( j ).getString (
									"geo" );
							if ( geo.equals ( "null" ) )
							{
								System.out.println ( "经纬度为空" );
								continue;
							}
							JSONObject geoObject = new JSONObject ( geo );
							if ( geoObject.getString ( "coordinates" ).equals (
									"null" ) )
							{
								System.out.println ( "经纬度为空" );
								continue;
							}
							double location[] = ProcessData.getGeo ( array
									.getJSONObject ( j ) );
							String collection_name = ProcessData.wkt_code (
									location [ 1 ], location [ 0 ], 9 );
							JSONObject json_array_data = array
									.getJSONObject ( j );
							data = (DBObject) com.mongodb.util.JSON
									.parse ( json_array_data.toString ( ) );
							OperMongo.saveData ( data, collection_name,
									table_name );
						}
						System.out.println ( "<----------------------->\r\n" );
					}
				}
			}
		}
		Statistics.statisticsWrite ( sim_whole, sim_error, sim_blank, 0, 0, 0 );
		System.gc ( );
	}
}
