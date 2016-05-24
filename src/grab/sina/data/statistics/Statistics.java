package grab.sina.data.statistics;


import grab.Adress;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLDecoder;

import json.JSONException;
import json.JSONObject;

/**
 * 
 * 提供一个统计抓取数据信息的类
 * 
 * 项目名称: BigDataGrab
 * 
 * 包: grab.sina.data.statistics
 * 
 * 类名称: Statistics
 * 
 * 类描述: 提供一个统计抓取数据信息的类
 * 
 * 创建人: 李佳骏
 * 
 * 创建时间: 2014年11月20日
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
public class Statistics
{
	
	static String	path	= Adress.exportStaPath;

	/**
	 * 
	 * 刷新统计信息
	 * 
	 * @throws NoSuchFieldException
	 * 
	 * @throws SecurityException
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 */
	static public void statisticsRefresh ( ) throws NoSuchFieldException,
			SecurityException, IOException, JSONException
	{
		// 读取过去统计信息
		String str_statistic_json = readFile ( path );
		JSONObject statistic_json = new JSONObject ( str_statistic_json );
		// 修改统计信息
		JSONObject grab_statistic = statistic_json
				.getJSONObject ( "grab_statistic" );
		grab_statistic.put ( "grab_num", 0 );
		grab_statistic.put ( "grab_error_num", 0 );
		grab_statistic.put ( "grab_blank_num", 0 );
		JSONObject db_statistic = statistic_json
				.getJSONObject ( "db_statistic" );
		db_statistic.put ( "db_num", 0 );
		db_statistic.put ( "collection_num", 0 );
		db_statistic.put ( "data_num", 0 );
		// 写入新的统计信息。
		statistic_json.put ( "grab_statistic", grab_statistic );
		statistic_json.put ( "db_statistic", db_statistic );
		String content = statistic_json.toString ( );
		writeFile(path,content);
	}

	/**
	 * 
	 * 提供读数据统计信息的函数
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 */
	static public JSONObject statisticsRead ( ) throws IOException,
			JSONException
	{
		// String str_statistic_json = new Statistics().readResource (
		// "/grab/sina/data/statistics/data_statistics.json" );
		String str_statistic_json = readFile ( path );
		JSONObject statistic_json = new JSONObject ( str_statistic_json );
		return statistic_json;
	}

	/**
	 * 
	 * 提供写统计信息的的函数
	 * 
	 * @param grab_num
	 * 
	 * @param grab_error_num
	 * 
	 * @param grab_blank_num
	 * 
	 * @param db_num
	 * 
	 * @param collection_num
	 * 
	 * @param data_num
	 * 
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @throws NoSuchFieldException
	 * 
	 * @throws SecurityException
	 * 
	 */
	static public void statisticsWrite ( int grab_num, int grab_error_num,
			int grab_blank_num, int db_num, int collection_num, int data_num )
			throws IOException, JSONException, NoSuchFieldException,
			SecurityException
	{
		// 读取过去统计信息
		String str_statistic_json = readFile ( path );
		JSONObject statistic_json = new JSONObject ( str_statistic_json );
		// 修改统计信息
		JSONObject grab_statistic = statistic_json
				.getJSONObject ( "grab_statistic" );
		grab_statistic.put ( "grab_num", grab_statistic.getInt ( "grab_num" )
				+ grab_num );
		grab_statistic.put ( "grab_error_num",
				grab_statistic.getInt ( "grab_error_num" ) + grab_error_num );
		grab_statistic.put ( "grab_blank_num",
				grab_statistic.getInt ( "grab_blank_num" ) + grab_blank_num );
		JSONObject db_statistic = statistic_json
				.getJSONObject ( "db_statistic" );
		db_statistic.put ( "db_num", db_num );
		db_statistic.put ( "collection_num", collection_num );
		db_statistic.put ( "data_num", data_num );
		// 写入新的统计信息。
		statistic_json.put ( "grab_statistic", grab_statistic );
		statistic_json.put ( "db_statistic", db_statistic );
		String content = statistic_json.toString ( );
		writeFile(path,content);
	}

	/**
	 * 在工程内部找文件路径，这种动态方式找到的路径可以再导出jar仍然可行。
	 * 
	 * @param path
	 * 
	 * @throws IOException
	 */
	private String findPath ( String path ) throws IOException
	{
		// 返回读取指定资源的输入流
		URL url = this.getClass ( ).getResource ( path );
		// URLl 对于中文会产生”乱码“，所以需要decode一下。
		return URLDecoder.decode ( url.getPath ( ).toString ( ), "utf-8" );
	}

	/**
	 * 在工程内部找配置文件，这种动态方式找到的路径可以再导出jar仍然可行。
	 * 
	 * @param path
	 * 
	 * @throws IOException
	 */
	private String readResource ( String path ) throws IOException
	{
		// 返回读取指定资源的输入流
		InputStream is = this.getClass ( ).getResourceAsStream ( path );
		BufferedReader br = new BufferedReader ( new InputStreamReader ( is ) );
		String line_str = "";
		String all_str = "";
		while ( ( line_str = br.readLine ( ) ) != null )
			all_str += line_str;
		return all_str;
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	private static String readFile ( String filePath )
	{
		String data = "";
		try
		{
			String encoding = "UTF-8";
			File file = new File ( filePath );
			if ( file.isFile ( ) && file.exists ( ) )
			{ // 判断文件是否存在
				InputStreamReader read = new InputStreamReader (
						new FileInputStream ( file ), encoding );// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader ( read );
				String lineTxt = null;
				while ( ( lineTxt = bufferedReader.readLine ( ) ) != null )
				{
					data += lineTxt;
				}
				read.close ( );
			}
			else
			{
				System.out.println ( "找不到指定的文件" );
			}
		}
		catch ( Exception e )
		{
			System.out.println ( "读取文件内容出错" );
			e.printStackTrace ( );
		}
		return data;
	}

	/**
	 * 
	 */
	private static void writeFile (String path, String content)
	{
		File file = new File ( path );
		try ( FileOutputStream fop = new FileOutputStream ( file ) )
		{
			// if file doesn't exists, then create it
			if ( !file.exists ( ) )
			{
				file.createNewFile ( );
			}
			// get the content in bytes
			byte [ ] contentInBytes = content.getBytes ( );
			fop.write(contentInBytes,0,contentInBytes.length);
			fop.flush ( );
			fop.close ( );
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
	}
}
