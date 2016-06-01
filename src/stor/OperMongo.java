
package stor;

import grab.sina.data.ProcessData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import json.JSONException;
import json.JSONObject;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

/**
 * 
 * 提供一系列的针对MongoDB的管理及操作函数。
 * 
 * @ProjectName: BigDataGrab
 * 
 * @Package: grab.sina.weibo.stor
 * 
 * @ClassName: OperMongo
 * 
 * @Description: MongoDB操作类
 * 
 * @Author: 李佳骏
 * 
 * @CreateDate: 2014.11.18
 * 
 * @UpdateUser: 无
 * 
 * @UpdateDate: 无
 * 
 * @UpdateRemark: 无
 * 
 * @Version: [v1.0]
 * 
 */
public class OperMongo
{	
	static Mongo	mongo	= null;
	//mongo = new Mongo ( );
	/**
	 * 连接数据库
	 * 
	 * 添加时间：2014年11月16日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public static boolean connectDB ( ) throws UnknownHostException
	{
		if ( null == mongo )
		{
			mongo = new Mongo ( "127.0.0.1", 27017 );
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 
	 * 关闭数据库
	 * 
	 * 添加时间：2014年11月16日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public static boolean closeDB ( ) throws UnknownHostException
	{
		if ( null != mongo )
		{
			mongo.close ( );
			mongo=null;
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * 获取数据库
	 * 
	 * 添加时间：2014年11月18日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param dbname
	 * @return
	 */
	public static DB getDB ( String dbname )
	{
		return mongo.getDB ( dbname );
	}

	/**
	 * 
	 * 将json格式的DBObject存入本地的mongodb中。
	 * 
	 * 添加时间：2014年11月01日
	 * 
	 * 添加人：李聪
	 * 
	 * 修改时间：2014年11月18日
	 * 
	 * 修改人：李佳骏
	 * 
	 * @param json_data
	 * 
	 * @param mongo
	 * 
	 * @param DB_name
	 * 
	 * @param collection_name
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException
	 * 
	 */
	public static void saveData ( DBObject data, String db_name,
			String collection_name ) throws JSONException, UnknownHostException
	{
		DB db = mongo.getDB ( db_name );
		DBCollection collection = db.getCollection ( collection_name );
		collection.find ( );
		collection.insert ( data );
	}

	/**
	 * 
	 * 将json格式的DBObject存入本地的mongodb中。
	 * 
	 * 添加时间：2014年11月21日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param json_data
	 * 
	 * @param mongo
	 * 
	 * @param DB_name
	 * 
	 * @param collection_name
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException
	 * 
	 */
	public static void saveData ( String data, String db_name,
			String collection_name ) throws JSONException, UnknownHostException
	{
		DB db = mongo.getDB ( db_name );
		DBCollection collection = db.getCollection ( collection_name );
		collection.find ( );
		collection.insert ( (DBObject) JSON.parse (data ) );
	}

	/**
	 * 
	 * 导出指定数据库下指定数据集的记录，以JSON格式，在指定根目录下，
	 * 按省-市-时间的文件结构存放好
	 * 
	 * 157808条数据(不联网处理)：164618ms 优化后164170ms 二次优化163869ms
 	 * 157808条数据(联网处理)：979631ms
 	 * 
	 * 添加时间：2014年12月01日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param output_path
	 *            输出地址
	 * @param time
	 *            数据时间
	 * @throws IOException
	 *             
	 * @throws JSONException 
	 */
	public static void export_date_pro_city_json (	String 					output_path,
																		String 					date ) 
																		throws	 				IOException, 
																									JSONException
	{
		int count = 0;
		//遍历读取，然后通过文件流写入
		String 				c_name 					=		"" ;
		String 				db_name 				=		"" ;
		Set < String > 	col_name_set 		= 		null ;
		List < String > 	db_name_list 			=	 	null ;
		DB 					db						=		null; 
		String 				write_path				=		"" ;
		JSONObject 		write_json_object 	= 		null ;
		File 					write_file				=		null ;
		FileWriter 			write_fw					=		null ;
		String[] 				ProCity					=		new String[2];
		
		db_name_list = mongo.getDatabaseNames ( ) ;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			db_name 			= 		db_name_list.get ( i ) ;
			db 					= 		mongo.getDB ( db_name ) ;
			col_name_set 	= 		db.getCollectionNames ( ) ;
			
			Iterator it = col_name_set.iterator ( );
			while ( it .hasNext ( ) )
			{
				c_name = it.next ( ).toString ( ) ;
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					DBCollection 		collection 		= 		db.getCollection ( c_name ) ;
					DBCursor 			cursor 			= 		collection.find ( ) ;
					cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
					while(cursor.hasNext ( ))
					{
						write_json_object 	= 		new JSONObject(cursor.next ( ).toString ( )) ;			
						ProCity 					= 		ProcessData.pcn_getProCityName ( write_json_object );
						write_json_object.remove ( "_id" ) ;
						System.out.println (count);
						write_path 				=  	output_path + "/" + date  + "/" + ProCity[0] ;
						write_file 				= 		new File ( write_path );
						if ( !write_file.isFile ( ) ) write_file.mkdirs  ( );
						write_fw 				= 		new FileWriter(write_path + "/" + ProCity[1] + ".json",true);
						write_fw.write( ( write_json_object.toString ( ) + "\r\n") );  
			            count++;
						write_fw.close();
						write_fw	= null;
					}
				}
			}	
		}
	}


	/**
	 * 自动识别日期。
	 * 导出指定数据库下指定数据集的记录，以JSON格式，在指定根目录下，
	 * 按省-市-时间的文件结构存放好
	 * 
	 * 157808条数据(不联网处理)：164618ms 优化后164170ms 二次优化163869ms
 	 * 157808条数据(联网处理)：979631ms
 	 * 
	 * 添加时间：2014年12月05日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @param output_path
	 *            输出地址
	 *            
	 * @throws IOException
	 *             
	 * @throws JSONException 
	 * 
	 * @throws ParseException 
	 * 
	 */
	public static void export_date_pro_city_json (	String 					output_path) 
																		throws	 				IOException, 
																									JSONException, ParseException
	{
		int count = 0;
		//遍历读取，然后通过文件流写入
		String 				c_name 					=		"" ;
		String 				db_name 				=		"" ;
		Set < String > 	col_name_set 		= 		null ;
		List < String > 	db_name_list 			=	 	null ;
		DB 					db						=		null; 
		String 				write_path				=		"" ;
		JSONObject 		write_json_object 	= 		null ;
		File 					write_file				=		null ;
		FileWriter 			write_fw					=		null ;
		String[] 				ProCity					=		new String[2] ;
		String				date						=		"" ;
		
		db_name_list = mongo.getDatabaseNames ( ) ;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			db_name 			= 		db_name_list.get ( i ) ;
			db 					= 		mongo.getDB ( db_name ) ;
			col_name_set 	= 		db.getCollectionNames ( ) ;
			
			Iterator it = col_name_set.iterator ( );
			while ( it .hasNext ( ) )
			{
				c_name = it.next ( ).toString ( ) ;
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					DBCollection 		collection 		= 		db.getCollection ( c_name ) ;
					DBCursor 			cursor 			= 		collection.find ( ) ;
					cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
					while(cursor.hasNext ( ))
					{
						write_json_object 	= 		new JSONObject(cursor.next ( ).toString ( )) ;			
						ProCity 					= 		ProcessData.pcn_getProCityName ( write_json_object ) ;
						date						=		write_json_object.getString ( "created_at" ) ;
						date						=		ProcessData.date_parse ( date ) ;
						write_json_object.remove ( "_id" ) ;
						System.out.println (count);
						write_path 				=  	output_path + "/" + date  + "/" + ProCity[0] ;
						write_file 				= 		new File ( write_path );
						if ( !write_file.isFile ( ) ) write_file.mkdirs  ( );
						write_fw 				= 		new FileWriter(write_path + "/" + ProCity[1] + ".json",true);
						write_fw.write( ( write_json_object.toString ( ) + "\r\n") );  
			            count++;
						write_fw.close();
						write_fw	= null;
					}
				}
			}	
		}
	}

	/**
	 * 导出
	 * 
	 * 添加时间：2014年11月01日
	 * 
	 * 添加人：李聪
	 * 
	 * 修改时间：2014年11月16日
	 * 
	 * 修改人：李佳骏
	 * 
	 * @param mongoexe_path
	 *            mongodb下bin的地址
	 * @param DB_name
	 *            库名
	 * @param collection_name
	 *            collection名
	 * @param output_path
	 *            输出地址
	 * @param time
	 *            数据时间
	 * @throws IOException
	 *             //执行指令:start G:\MyMongoDB\bin\mongoexport -d 2014-2-27 -c
	 *             2014-2-27 E:\MySinaWeiboData\export2014-2-27\2014-2-27-0.js
	 */
	public static void export_json ( 	String 					DB_name,
													String 					collection_name,
													String 					output_path,
													String 					time ) 
													throws	 				IOException
	{
		File newfile = new File ( output_path + "\\" + time );
		if ( !newfile.isFile ( ) )
		{
			newfile.mkdir ( );
		}
		String bat = "start " + "mongoexport -d " + DB_name + " -c "
				+ collection_name + " -o " + output_path + "\\" + time + "\\"
				+ collection_name + ".json";
		Runtime.getRuntime ( ).exec ( "cmd /c " + bat );
	}

	/**
	 * 函数描述：删除MongoDB内所有的重复数据。
	 *  
	 * 添加日期：2014年11月22日 
	 * 
	 * 添加人：李佳骏 
	 * 
	 * 修改日期：2014年12月11日
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改内容：提高去重过程中内存消耗正比持续增长的BUG
	 * 
	 * 性能测试结果：处理200777条数据，耗时152508ms，处理后剩余数据110457条。
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException 
	 */
	public static void dupliDeleteAll ( ) throws JSONException, UnknownHostException
	{
		int all = 0;
		List < String > 	db_name_list 		= 		mongo.getDatabaseNames ( );
		Set < String > 	col_name_set 	=		null ;
		String 				db_name 			= 		null ;
		String 				c_name				=		null ;
		DB 					db					=		null ;
		Iterator 			it						=		null ;
		int 					repeat 				= 		0 ;
		int					free_count			=		0 ;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			db_name 			= 		db_name_list.get ( i );
			db 					= 		mongo.getDB ( db_name );
			col_name_set 	= 		db.getCollectionNames ( );
			it 						= 		col_name_set.iterator ( );
			while ( it.hasNext ( ) )
			{
				c_name = it.next ( ).toString ( );
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					repeat = 0 ;
					System.out.print ( "正在清理："+db_name+"中的"+c_name+"." );
					repeat = dupliDelete(db_name, c_name);
					if( 0 == free_count%8 )mem_Free(  ) ;
					free_count++ ;
					System.out.println ( "其中有重复记录："+repeat+"条.	清理完毕！" );
					all+=repeat;
				}
			}
		}
		System.out.println ( "去重结束！一共去重："+all+"条." );
		mem_Free();
	}
	
	/**
	 * 函数描述：删除重复数据。
	 *  
	 * 添加日期：2014年11月18日 
	 * 
	 * 添加人：李佳骏 
	 * 
	 * 修改日期：2014年11月19日 
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改日期：2014年11月23日 
	 * 
	 * 修改人：李佳骏
	 * 
	 * 修改内容：添返回加数据重复量
	 * 
	 * 性能测试结果：处理50616条数据(其中有5W条重复数据)，耗时17968ms.
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException 
	 */
	public static int dupliDelete ( String db_name, String collection_name )
			throws JSONException, UnknownHostException
	{
		int repeat = 0;
		DB db = mongo.getDB ( db_name );
		DBCollection collection = db.getCollection ( collection_name );
		List < String > id_list = collection.distinct ( "id" );// 获取一张ID表，这张表的字段都是唯一不重复的。
		DBCursor cursor = collection.find ( );// 获取表指针。
		boolean isDupli = false;// 判断是否是重复数据的标识。
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while ( cursor.hasNext ( ) )
		{
			DBObject dbo 	= 		cursor.next ( );
			JSONObject js 	= 		new JSONObject ( dbo.toString ( ) );
			isDupli 				= 		false;
			if ( 0 == id_list.size ( ) )// 如果唯一ID里面没有记录，后面的数据全部为重复数据。
			{
				collection.remove ( dbo );// 在唯一ID表中这个记录出现了超过一次，是重复数据，执行删除。
			}
			for ( int i = 0; i < id_list.size ( ); i++ )
			{
				if ( js.get ( "id" ).toString ( )
						.equals ( id_list.get ( i ).toString ( ) ) )// 找到和唯一ID表匹配的记录
				{
					id_list.remove ( i );// 删除这个唯一ID表的记录
					isDupli = false;// 匹配上就说明不重复
					break;// 不重复就可以不用往下找
				}
				else
				{
					isDupli = true;// 找不到很可能重复，继续往下找，直到最后一条记录
				}
			}
			// 经过上面的判断后，可以断定改数据是否为重复数据。
			if ( isDupli )// 如果重复
			{
				collection.remove ( dbo );// 在唯一ID表中这个记录出现了超过一次，是重复数据，删除
				repeat++;
			}
		}
		mem_Free();
		return repeat;
	}

	/**
	 * 
	 * 计算指定数据库下的指定数据集的数据重复率
	 *  
	 * 添加日期：2014年11月18日 
	 * 
	 * 添加人：李佳骏 
	 * 
	 * @param db_name
	 * 
	 * @param collection_name
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException 
	 * 
	 */
	public static void dupliRateCountAll(  ) throws JSONException, UnknownHostException 
	{
		connectDB() ;
		List < String > 	db_name_list 		= 		mongo.getDatabaseNames ( );
		Set < String > 	col_name_set 	=		null ;
		String 				db_name 			= 		null ;
		String 				c_name				=		null ;
		DB 					db					=		null ;
		Iterator 			it						=		null ;
		int					free_count			=		0 ;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			db_name = db_name_list.get ( i );
			db = mongo.getDB ( db_name );
			col_name_set = db.getCollectionNames ( );
			it = col_name_set.iterator ( );
			while ( it.hasNext ( ) )
			{
				c_name = it.next ( ).toString ( );
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					System.out.print ( "正在计算："+ db_name + "中的" + c_name + "." ) ;
					System.out.println ( "其中有重复率为：" + dupliRateCount( db_name, c_name )*100 + "%" ) ;
					if( 0 == free_count % 8 )mem_Free(  ) ;
					free_count++ ;
				}
			}
		}
		mem_Free(  );
	}
	
	/**
	 * 
	 * 计算指定数据库下的指定数据集的数据重复率
	 *  
	 * 添加日期：2014年11月18日 
	 * 
	 * 添加人：李佳骏 
	 * 
	 * @param db_name
	 * 
	 * @param collection_name
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * 
	 * @throws UnknownHostException 
	 * 
	 */
	public static double dupliRateCount( String db_name, String collection_name ) throws JSONException, UnknownHostException 
	{
		double rate = 0 ;
		DB db = mongo.getDB ( db_name );
		DBCollection collection = db.getCollection ( collection_name );	
		rate = (double)( collection.getCount ( ) - collection.distinct ( "id" ).size ( ) ) / collection.getCount ( ) ;
		return  rate ;
	}
	
	/**
	 * 获取数据库数目
	 * 
	 * 添加时间：2014年11月20日
	 * 
	 * 添加人：李佳骏 
	 * 
	 * @return
	 */
	public static int getDBNum ( )
	{
		return mongo.getDatabaseNames ( ).size ( ) - 1;
	}

	/**
	 * 获取数据集数目
	 * 
	 * 添加时间：2014年11月20日
	 * 
	 * 添加人：李佳骏 
	 * 
	 * 修改时间：2014年11月30日
	 * 
	 * 修改人：李佳骏 
	 * 
	 * @return
	 */
	public static int getCollectionNum ( )
	{
		List < String > db_name_list = mongo.getDatabaseNames ( );
		int c_num = 0;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			String db_name =  db_name_list.get ( i ) ;
			if ( db_name.equals ( "local" ) )
				continue;
			else if ( db_name.equals ( "system.indexes" ) )
				continue;
			if ( db_name.equals ( "admin" ) )
				continue;
			else
				c_num += (mongo.getDB ( db_name ).getCollectionNames ( ).size ( ) - 1);
		}
		return c_num;
	}

	/**
	 * 获取数据记录数目
	 * 
	 * 添加时间：2014年11月20日
	 * 
	 * 添加人：李佳骏 
	 * 
	 * @return
	 */
	public static int getDataNum ( )
	{
		List < String > db_name_list = mongo.getDatabaseNames ( );
		int d_num = 0;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			String db_name = db_name_list.get ( i );
			DB db = mongo.getDB ( db_name );
			Set < String > col_name_set = db.getCollectionNames ( );
			Iterator it = col_name_set.iterator ( );
			while ( it.hasNext ( ) )
			{
				String c_name = it.next ( ).toString ( );
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					d_num += db.getCollection ( c_name ).count ( );
				}
			}
		}
		return d_num;
	}
	

	/**
	 * 
	 */
	public static double getDBCapacity()
	{
		double capacity = 0;
		List < String > db_name_list = mongo.getDatabaseNames ( );
		
		System.out.println(mongo.getAddress ( ).toString ( ));
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			String d_name = mongo.getDB ( db_name_list.get ( i ) )
					.getCollectionNames ( ).iterator ( ).next ( ).toString ( );
			if ( "local" != d_name ){}
				//c_num += (mongo.getDB ( db_name_list.get ( i ) ).);
		}
		return capacity;
	}
	
	/**
	 * 删除一个数据库
	 * @param name
	 */
	public static void deleteDB(String db_name)
	{
		mongo.dropDatabase ( db_name );
	}
	
	/**
	 *  删除所有数据库，除了local
	 */
	public static void deleteDBAll()
	{
		List < String > db_name_list = mongo.getDatabaseNames ( );
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			String d_name = db_name_list.get ( i ).toString ( );
			if ( !d_name.equals("local") )
				deleteDB(d_name);
		}
	}
	
	
	/**
	 * 
	 * 释放Mongodb占用的内存
	 * 
	 * 添加日期：2014年12月12日
	 * 
	 * 添加人：李佳骏
	 * 
	 * @throws UnknownHostException 
	 * 
	 */
	public static void mem_Free( ) throws UnknownHostException 
	{
		mongo.getDB ( "admin" ).command ( "closeAllDatabases" ) ;
	}
	
	
	public static void main(String[] args) throws JSONException, IOException
	{
		OperMongo.connectDB ( );
		//System.out.println ( OperMongo.getDataNum ( ));
		//OperMongo.dupliDeleteAll ( );
		long start = System.currentTimeMillis ( );
		try
		{
			//OperMongo.dupliRateCountAll ( ) ;
			//OperMongo.dupliDeleteAll ( );
			OperMongo.deleteDBAll();
			//OperMongo.export_date_pro_city_json ( "/Users/xgxy009/Documents/BDG/export" ) ;
			//OperMongo.mem_Get  ( );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis ( );
		System.out.println("耗时：" + (end-start) + "ms");

		System.out.println ("结束");
		OperMongo.closeDB ( ) ;
		/*
		OperMongo.connectDB ( );
		List < String > db_name_list = mongo.getDatabaseNames ( );
		int count = 0;
		for ( int i = 0; i < db_name_list.size ( ); i++ )
		{
			String db_name = db_name_list.get ( i );
			DB db = mongo.getDB ( db_name );
			Set < String > col_name_set = db.getCollectionNames ( );
			Iterator it = col_name_set.iterator ( );
			while ( it.hasNext ( ) )
			{
				String c_name = it.next ( ).toString ( );
				if ( !c_name.equals ( "system.indexes" ) &&!c_name.equals ( "startup_log" ))
				{
					int temp = getDataNumByKey(db_name,c_name,"双十一") + getDataNumByKey(db_name,c_name,"双11");
					count = count + temp;
					System.out.println ( db_name+"下的"+c_name+"中有"+count+"条." );
					System.out.println ( "累计"+count+"条." );
					
				}
			}
		}
		*/
		/*
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR, 1);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		long ss = (c.getTime().getTime() - System.currentTimeMillis())/1000;
		
		String data =new OperMongo().readResource("/grab/1.txt");
		System.out.println(ss);
		SimpleDateFormat df = new SimpleDateFormat (
				"yyyy-MM-dd HH:mm:ss" );// 设置日期格式
		
		for(int j = 0; j<10; j++)
		{
			long start = System.currentTimeMillis ( );
			OperMongo.connectDB ( );
			for(int i = 0; i<500; i++)
			{
				OperMongo.saveData ( data, "test_ddb", "hh" + (j*500 + i) );
			}
			OperMongo.closeDB ( );
			long end = System.currentTimeMillis ( );
			System.out.println(j*500+500+"个。耗时：" + (end-start) + "ms");
		}
		*/
	}
}
