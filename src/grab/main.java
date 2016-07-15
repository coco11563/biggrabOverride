
package grab;


import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

import mail.EmailSend;
import json.JSONException;
import json.JSONObject;
import stor.OperMongo;

import com.mongodb.DB;
/*
import grab.importdata.HbaseImport;*/
import grab.importdata.MySQLImport;
import grab.sina.config.readConfig;
import grab.sina.data.AreaData;
import grab.sina.data.GetData;
import grab.sina.data.Point;
import grab.sina.data.ProcessData;
import grab.sina.data.statistics.Statistics;

public class main
{
	public static void main ( String [ ] args ) 	
	{		
		
		try {
			OperMongo.connectDB ( ) ;
			//清空一天的数据
			OperMongo.deleteDBAll();
			
			OperMongo.closeDB ( ) ;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		long start_all = System.currentTimeMillis ( );
		try
		{
			
			LinkedList < AreaData > location = new LinkedList < AreaData > ( );
			LinkedList < AreaData > meticulouslocation = new LinkedList < AreaData > ( );//精细抓取区域，计划包涵北上广深武汉杭州
			location.add ( new AreaData ( 39, 41, 111, 118.5 ) );// 京津唐冀地区
			location.add ( new AreaData ( 27.5, 32, 120.5, 122 ) );// 沪杭
			location.add ( new AreaData ( 22, 24.25, 107, 114.5 ) );// 两广
			location.add ( new AreaData ( 24.25, 37, 117.75, 119 ) );
			location.add ( new AreaData ( 27.25, 37, 119, 120.5 ) );
			location.add ( new AreaData ( 24.25, 31, 107, 117.75 ) );// 大地区
			location.add ( new AreaData ( 31, 39, 107, 117.75 ) );// 大地区
			location.add ( new AreaData ( 43, 48, 120, 131 ) );// 东三省
			location.add ( new AreaData ( 41, 43, 114, 126 ) );// 东三省
			location.add ( new AreaData ( 40, 41, 122.25, 124.5 ) );// 东三省
			location.add ( new AreaData ( 38.75, 39, 121.25, 122.25 ) );// 东三省
			location.add ( new AreaData ( 27, 32.5, 103, 107 ) );// 川黔渝
			location.add ( new AreaData ( 23.5, 27, 99, 107 ) );// 滇
			
			//20151113 licong
			location.add ( new AreaData ( 43.65, 44.5, 87.2, 87.8 ) );//乌鲁木齐
			location.add ( new AreaData ( 32.5, 42.5, 98, 107 ) );//青海
			location.add ( new AreaData ( 40.3, 40.7, 79.7, 82 ) );//阿克苏
			location.add ( new AreaData ( 40.7, 41.4, 79.9, 80.7 ) );//
			location.add ( new AreaData ( 41.2, 41.8, 85.7, 86.3 ) );//巴音郭楞
			location.add ( new AreaData ( 41.8, 42.3, 86.1, 87.3 ) );//
			location.add ( new AreaData ( 44.8, 45, 82, 82.55 ) );//阿勒泰
			location.add ( new AreaData ( 47.75, 47.95, 88.1, 88.2 ) );//
			location.add ( new AreaData ( 37, 37.5, 79.4, 80.4 ) );//和田
			location.add ( new AreaData ( 29.6, 29.75, 90.9, 91.45 ) );//拉萨
			
			//20151118 licong
			location.add ( new AreaData ( 18.1, 20.15, 108.5, 111 ) );// 海南
			location.add ( new AreaData ( 22.5, 24.25, 120, 120.5 ) );// 台湾
			location.add ( new AreaData ( 22, 24.75, 120.5, 121 ) );// 台湾
			location.add ( new AreaData ( 22.75, 25.25, 121, 121.5 ) );// 台湾
			location.add ( new AreaData ( 24.25, 25.25, 121.5, 122 ) );// 台湾
			
		meticulouslocation.add ( new AreaData ( 30.2, 30.9, 113.9, 114.7 ) );	//武汉
		meticulouslocation.add ( new AreaData ( 39.46,40.22,115.83,116.91 ) ); 	//北京
		meticulouslocation.add ( new AreaData ( 30.69,31.66,120.98,121.95 ) );	//上海
		meticulouslocation.add ( new AreaData ( 23.0,23.21,113.21,113.46 ) );	//广州
		meticulouslocation.add ( new AreaData ( 22.86,22.55,113.64,114.59) );	//深圳
		meticulouslocation.add ( new AreaData ( 30.06,30.42,119.85,120.43 ) );	//杭州

			
			
			double lat_min;
			double lon_min;
			double lat_max;
			double lon_max;
			//精细化抓取区域
			double mlat_min;
			double mlon_min;
			double mlat_max;
			double mlon_max;
			long unix_start_time;
			long unix_end_time;
		
			String collection_name;
			String [ ] date_conf = readConfig.read_date_config ( );
			String start_time = date_conf [ 0 ];
			String last_days = date_conf [ 1 ];
			String continued_days = date_conf[ 2 ];
			double blank_get = 0 ;
			int grab_blank = 0 ;
			//创建邮件发送对象。
			EmailSend email_send = new EmailSend();
			//统计信息刷新。
			Statistics.statisticsRefresh ( );
			for ( int i = 0; i <  Integer.parseInt(last_days); i++ )
			{
				//为了实现在程序不中止的情况下可以更改日期，抓完当天后生效
				date_conf = readConfig.read_date_config ( );
				start_time = date_conf [ 0 ];
				last_days = date_conf [ 1 ];
				continued_days = date_conf[ 2 ];
				SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd" );
				Date date = sdf.parse (start_time);
				Calendar c = Calendar.getInstance ( );
				c.setTime ( date );
//				c.add ( Calendar.DATE, i );
				c.add ( Calendar.DATE, 0 );
				String time_start = sdf.format ( c.getTime ( ) ) + " 00:00:00";
				String time_end = sdf.format ( c.getTime ( ) ) + " 23:59:59";
				unix_start_time = ProcessData.unixTime ( time_start );
				unix_end_time = ProcessData.unixTime ( time_end );
				collection_name = time_end.substring ( 0, 10 );
				
				/**********************************************************************************/
				/********************如果是抓当天的数据，则等待到下一天早上8点开始工作。**********************/
				/**/int y,m,d,h,mi,s;  															/**/  
				/**/Calendar cal=Calendar.getInstance();     									/**/
				/**/if( collection_name.equals ( sdf.format ( cal.getTime ( ) ) ) )				/**/
				/**/{																			/**/
				/**/	long delay = 0;															/**/
				/**/	c.set(Calendar.HOUR_OF_DAY, 23);										/**/
				/**/	c.set(Calendar.MINUTE, 59);												/**/
				/**/	c.set(Calendar.SECOND, 59);												/**/
				/**/	//计算当前离今天23:59:59有多少毫秒											/**/
				/**/	delay = c.getTimeInMillis() - System.currentTimeMillis();				/**/
				/**/	//加一秒到下一天，再加上8小时的毫秒											/**/
				/**/	delay = delay + 1000 + 60*60*8*1000;									/**/
				/**/	System.out.println("昨天的数据已经取完,休眠到明天早上8天,之后开始抓去今天的数据.");	/**/
				/**/	Thread.sleep(delay);													/**/
				/**/}																			/**/
				/******************************JiaJun Lee,2015.01.07*******************************/
				/**********************************************************************************/
				LinkedList<Point> swap = new LinkedList<Point>();
				if(Integer.parseInt(continued_days) == 0)//首次执行初始化矩阵
				{
				for ( int j = 0; j < location.size ( ); j++ )
				{
					OperMongo.connectDB ( );
					AreaData lat_lon = location.get ( j );
					lat_min = lat_lon.getLat_min ( );
					lon_min = lat_lon.getLon_min ( );
					lat_max = lat_lon.getLat_max ( );
					lon_max = lat_lon.getLon_max ( );
					///////////////////////////////////////////////////////////////////////////////////////////////////
					//发送一封邮件告知开始抓取新地区
					JSONObject statistic_json =  Statistics.statisticsRead ( );
					JSONObject grab_statistic = statistic_json
							.getJSONObject ( "grab_statistic" );
					JSONObject db_statistic = statistic_json
							.getJSONObject ( "db_statistic" );
					SimpleDateFormat df = new SimpleDateFormat (
							"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
					String email_addresses[] = readConfig.read_email_address_config ( );//发送前读取可以在程序运转过程中添加服务邮箱。
					
					
					 
					//要求降低信息发送频率，定制一额发送
					//有的时候库里只有14个
					if(0==j || 5==j|| 9==j || 15==j )
					{
						for(int mailnum = 0 ;mailnum< email_addresses.length;mailnum++)
						{
						email_send.EmailSendByAddress(email_addresses[mailnum], df, sdf, grab_statistic, c, email_send);
						}
						}
					///////////////////////////////////////////////////////////////////////////////////////////////////
					
//					GetData.getSinaData_wkt_wkt_time_lite ( collection_name, lat_min, lon_min, lat_max,lon_max, unix_start_time, unix_end_time);
					swap.addAll(GetData.getSinaData_init_list( collection_name, lat_min, lon_min, lat_max,lon_max, unix_start_time, unix_end_time));
					readConfig.writePointType(swap);
					OperMongo.closeDB ( );
					grab_blank = grab_statistic.getInt ( "grab_blank_num" );
				}
			}
				else if(Integer.parseInt(continued_days) <= 7)//七天训练
				{
					swap = readConfig.readPointType();
					LinkedList<Point> store = new LinkedList<Point>();
					for ( int j = 0; j < location.size ( ); j++ )
					{
						OperMongo.connectDB ( );
						AreaData lat_lon = location.get ( j );
						lat_min = lat_lon.getLat_min ( );
						lon_min = lat_lon.getLon_min ( );
						lat_max = lat_lon.getLat_max ( );
						lon_max = lat_lon.getLon_max ( );
						///////////////////////////////////////////////////////////////////////////////////////////////////
						//发送一封邮件告知开始抓取新地区
						JSONObject statistic_json =  Statistics.statisticsRead ( );
						JSONObject grab_statistic = statistic_json
								.getJSONObject ( "grab_statistic" );
						JSONObject db_statistic = statistic_json
								.getJSONObject ( "db_statistic" );
						SimpleDateFormat df = new SimpleDateFormat (
								"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
						String email_addresses[] = readConfig.read_email_address_config ( );//发送前读取可以在程序运转过程中添加服务邮箱。
						
						
						 
						//要求降低信息发送频率，定制一额发送
						//有的时候库里只有14个
						if(0==j || 5==j|| 9==j || 15==j )
						{
							for(int mailnum = 0 ;mailnum< email_addresses.length;mailnum++)
							{
							email_send.EmailSendByAddress(email_addresses[mailnum], df, sdf, grab_statistic, c, email_send);
							}
							}
						///////////////////////////////////////////////////////////////////////////////////////////////////
						
//						GetData.getSinaData_wkt_wkt_time_lite ( collection_name, lat_min, lon_min, lat_max,lon_max, unix_start_time, unix_end_time);
						store = GetData.getSinaData_train_list(swap, collection_name, lat_min, lon_min, lat_max, lon_max, unix_start_time, unix_end_time);
						readConfig.writePointType(store);
						OperMongo.closeDB ( );
						grab_blank = grab_statistic.getInt ( "grab_blank_num" );
					}
				}
				else if(Integer.parseInt(continued_days) > 7&&Integer.parseInt(continued_days)<=30)//30天使用
				{
					swap = readConfig.readPointType();
					
					for ( int j = 0; j < location.size ( ); j++ )
					{
						OperMongo.connectDB ( );
						AreaData lat_lon = location.get ( j );
						lat_min = lat_lon.getLat_min ( );
						lon_min = lat_lon.getLon_min ( );
						lat_max = lat_lon.getLat_max ( );
						lon_max = lat_lon.getLon_max ( );
						///////////////////////////////////////////////////////////////////////////////////////////////////
						//发送一封邮件告知开始抓取新地区
						JSONObject statistic_json =  Statistics.statisticsRead ( );
						JSONObject grab_statistic = statistic_json
								.getJSONObject ( "grab_statistic" );
						JSONObject db_statistic = statistic_json
								.getJSONObject ( "db_statistic" );
						SimpleDateFormat df = new SimpleDateFormat (
								"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
						String email_addresses[] = readConfig.read_email_address_config ( );//发送前读取可以在程序运转过程中添加服务邮箱。
						
						
						 
						//要求降低信息发送频率，定制一额发送
						//有的时候库里只有14个
						if(0==j || 5==j|| 9==j || 15==j )
						{
							for(int mailnum = 0 ;mailnum< email_addresses.length;mailnum++)
							{
							email_send.EmailSendByAddress(email_addresses[mailnum], df, sdf, grab_statistic, c, email_send);
							}
							}
						///////////////////////////////////////////////////////////////////////////////////////////////////
						
//						GetData.getSinaData_wkt_wkt_time_lite ( collection_name, lat_min, lon_min, lat_max,lon_max, unix_start_time, unix_end_time);
						GetData.getSinaData_use_list(swap, collection_name,  unix_start_time, unix_end_time);
						
						OperMongo.closeDB ( );
						grab_blank = grab_statistic.getInt ( "grab_blank_num" );
					}
				}
				//coco1 add at 2016年6月1日20:26:22
				//针对某几个区域进行精细抓取，抓取半径设置为5000
//				for ( int j1 = 0; j1 < meticulouslocation.size ( ); j1++ )
//				{
//					OperMongo.connectDB();
//					AreaData mlat_lon = meticulouslocation.get(j1);
//					mlat_min = mlat_lon.getLat_min ( );
//					mlon_min = mlat_lon.getLon_min ( );
//					mlat_max = mlat_lon.getLat_max ( );
//					mlon_max = mlat_lon.getLon_max ( );
//					GetData.getSinaData_new_test( collection_name, mlat_min, mlon_min, mlat_max,mlon_max, unix_start_time, unix_end_time,6000);
//					
//					OperMongo.closeDB ( );
//					
//					 
//				}
				
				//数据去重
				OperMongo.connectDB ( ) ;
				
				long start = System.currentTimeMillis ( );
				OperMongo.dupliDeleteAll();
				long end = System.currentTimeMillis ( );
				 
				OperMongo.closeDB ( ) ;
				String email_addresses[] = readConfig.read_email_address_config ( );
				for(int mailnum = 0 ;mailnum< email_addresses.length;mailnum++)
				{
					email_send.EmailSendByAddress(end-start, email_addresses[mailnum], email_send);
				}
				//数据导出
				OperMongo.connectDB ( ) ;
				start = System.currentTimeMillis ( );
				OperMongo.export_date_pro_city_json ( Adress.exportJsonPath ) ;				
				end = System.currentTimeMillis ( );
				long end_all = System.currentTimeMillis ( );
				for(int mailnum = 0 ;mailnum< email_addresses.length;mailnum++)
				{
					email_send.EmailSendByAddress((end-start)/1000,(end_all - start_all)/(1000 * 60) , email_addresses[mailnum], email_send,blank_get,grab_blank);
				}
				
				
				//清空一天的数据
				OperMongo.deleteDBAll();
				
				OperMongo.closeDB ( ) ;
				//配置文件中的日期也增加一天，下次启动便不用修改配置文件
				readConfig.add_one_day_in_config();

			}
		}
		catch ( UnknownHostException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( JSONException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( ParseException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( SQLException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( NoSuchFieldException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( SecurityException e )
		{
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}
		catch ( InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
