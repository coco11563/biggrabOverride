
package grab.sina.config;


import grab.sina.data.ProcessData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

/**
 * 
 * 读取配置文件的类,提供access_token读取和抓取date时间的读取函数。
 * 
 * @ProjectName: BigDataGrab
 * 
 * @Package: grab.sina.weibo.config
 * 
 * @ClassName: readConfig
 * 
 * @Description: 读取配置文件的类
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
/**
 * 更改时间：2015.6.7
 * 将配置文件路径设置成jar包外
 * @author jiajunlee
 *
 */
public class readConfig
{
	public static void main(String[] args)
	{
		try {
			add_one_day_in_config();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String date_path = "/home/biggrab/config/date_config.json";
	public static String token_path = "/home/biggrab/config/sina_token.json";
	public static String email_path = "/home/biggrab/config/email_address_config.json";
	
	public static void add_one_day_in_config() throws IOException, JSONException, ParseException {
		
//		String str_date_config_json = new readConfig()
//				.readResource("/grab/sina/config/date_config.json");
		String str_date_config_json = readFile(date_path);
		JSONObject date_config_json = new JSONObject(str_date_config_json);
		JSONObject date_json = date_config_json.getJSONObject("date");
		String start_time = date_json.getString("start_time");
		
		SimpleDateFormat sdf 	= new SimpleDateFormat ( "yyyy-MM-dd" );
		Date date 						= sdf.parse (start_time);
		Calendar c 					= Calendar.getInstance ( );
		c.setTime ( date );
		c.add ( Calendar.DATE, 1 );
		String time_start 			= sdf.format ( c.getTime ( ) );
		
		date_json.put("start_time", time_start);
		date_config_json.put("date", date_json);
		
		//写文件
		writeFile(date_path,date_config_json.toString());
	}
	
	/**
	 * 读取时间配置文件的函数。
	 * 
	 * @return
	 * 
	 * @throws JSONException
	 * 
	 * @throws IOException
	 * 
	 */
	public static String [ ] read_date_config ( ) throws JSONException,
			IOException
	{
		System.out.println(date_path);
		String str_date_config_json = readFile( date_path );
		JSONObject date_config_json = new JSONObject ( str_date_config_json );
		JSONObject date_json = date_config_json.getJSONObject ( "date" );
		String [ ] date = new String [ 2 ];
		date [ 0 ] = date_json.getString ( "start_time" );
		date [ 1 ] = date_json.getString ( "last_days" );
		return date;
	}

	/**
	 * @throws IOException
	 * 
	 * @throws JSONException
	 * 
	 * @Title:access_token读取函数
	 * 
	 * @Description: 读取access_token的配置文件
	 * 
	 * @return String[]
	 * 
	 * @throws
	 */
	public static String [ ] read_access_token_config ( ) throws JSONException,
			IOException
	{
		//		String str_access_token_config_json = new readConfig ( )

//				.readResource ( "/grab/sina/config/access_token_config.json" );

//		JSONObject access_token_config_json = new JSONObject (

//				str_access_token_config_json );

//		JSONObject access_token_json = access_token_config_json

//				.getJSONObject ( "access_token" );

//		String [ ] access_token = new String [ access_token_json.length ( ) ];

//		String str_key = "access_token_";

//		for ( int i = 1; i <= access_token_json.length ( ); i++ )

//		{

//			access_token [ i - 1 ] = access_token_json.getString ( str_key + i )

//					.toString ( );

//		}

//		return access_token;

		

		String str_access_token_config_json = readFile(token_path);

		JSONObject access_token_config_json = new JSONObject(

				str_access_token_config_json);

		JSONArray access_token_json = access_token_config_json

				.getJSONArray("access_token");

		String[] access_token = new String[access_token_json.length()];

		for (int i = 0; i < access_token_json.length(); i++) {

			access_token[i] = access_token_json.getString(i).toString();

		}

		return access_token;

	}

	/**
	 * 文件读取函数
	 * 
	 * @param path
	 * 
	 * @param encoding
	 * 
	 * @return
	 */
	private static String readFile ( String path, String encoding )
	{
		String content = "";
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader ( new InputStreamReader (
					new FileInputStream ( path ), encoding ) );
			String str = "";
			while ( ( str = reader.readLine ( ) ) != null )
			{
				content += str;
			}
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace ( );
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace ( );
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
		finally
		{
			if ( reader != null )
			{
				try
				{
					reader.close ( );
				}
				catch ( IOException e )
				{
					e.printStackTrace ( );
				}
			}
		}
		return content;
	}

	public static String [ ] read_email_address_config ( ) throws JSONException,
			IOException
	{
		String str_email_address_config_json = readFile( email_path );
		JSONObject email_address_config_json = new JSONObject (
				str_email_address_config_json );
		JSONObject email_address_json = email_address_config_json
				.getJSONObject ( "email_address" );
		String [ ] email_address = new String [ email_address_json.length ( ) ];
		String str_key = "email_address_";
		for ( int i = 1; i <= email_address_json.length ( ); i++ )
		{
			email_address [ i - 1 ] = email_address_json.getString ( str_key + i )
					.toString ( );
		}
		return email_address;
	}

//	/**
//	 * 在工程内部找配置文件，这种动态方式找到的路径可以再导出jar仍然可行。
//	 * 
//	 * @param path
//	 * 
//	 * @throws IOException
//	 */
//	public String readResource ( String path ) throws IOException
//	{
//		// 返回读取指定资源的输入流
//		InputStream is = this.getClass ( ).getResourceAsStream ( path );
//		BufferedReader br = new BufferedReader ( new InputStreamReader ( is ) );
//		String line_str = "";
//		String all_str = "";
//		while ( ( line_str = br.readLine ( ) ) != null )
//			all_str += line_str;
//		return all_str;
//	}
//	
//	/**
//	 * 在工程内部找配置文件，这种动态方式找到的路径可以再导出jar仍然可行。
//	 * 
//	 * @param path
//	 * 
//	 * @throws IOException
//	 */
//	public static void writeResource ( String path,String data ) throws IOException
//	{	
//
//		File file = new File(readConfig.class.getClassLoader().getResource("grab/sina/config/date_config.json").getFile());
//		FileWriter fw = new FileWriter(file.getAbsoluteFile());
//		BufferedWriter bw = new BufferedWriter(fw);
//		bw.write(data);
//		bw.close();
//	}
	
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
	 * 写文件函数
	 * 
	 * 添加日期：2014年11月30日
	 * 
	 * 添加人：李佳骏
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
			fop.write ( contentInBytes,0,contentInBytes.length );
			fop.flush ( );
			fop.close ( );
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
	}
}