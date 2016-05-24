
package seg.demo;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.JSONException;
import json.JSONObject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import stor.OperMongo;

public class IKAnalyzerDemo
{

	public static void main ( String [ ] args ) throws IOException
	{
		int count = 50000;
		int num = 0;
		String path = "D:\\seg";
		try
		{
			Mongo mongo = new Mongo ( );
			List < String > db_name_list = mongo.getDatabaseNames ( );
			// 数据库遍历
			for ( int i = 0; i < db_name_list.size ( ); i++ )
			{
				String db_name = db_name_list.get ( i );
				DB db = mongo.getDB ( db_name );
				Set < String > col_name_set = db.getCollectionNames ( );
				Iterator it = col_name_set.iterator ( );
				// 数据集遍历
				while ( it.hasNext ( ) )
				{
					String collection_name = it.next ( ).toString ( );
					if ( !collection_name.equals ( "system.indexes" )
							&& !collection_name.equals ( "startup_log" ) )
					{
						DBCollection collection = db
								.getCollection ( collection_name );
						DBCursor cursor = collection.find ( );// 获取表指针。
						// 记录遍历
						while ( cursor.hasNext ( ) )
						{
							DBObject dbo = cursor.next ( );
							JSONObject js = new JSONObject ( dbo.toString ( ) );
							JSONObject new_js = new JSONObject ( );
							// 分词
							//String text = js.getString ( "text" );
							String text = "5万3亿";
							System.out.println(count +  ":" +text);
							if(text.equals ( "" ))continue;
							//清理无用内容
							text = handlehttp(text);
							text = handleemo(text);
							text = handleurl(text);
							text = handleat(text); 
							text = ChnToAlb.bulidTextZHToALB(text);
							System.out.println(count +  ":" +text);
							// 分词
							text = segAnalyzer(text);
							new_js.put ( "text", text );
							new_js.put ( "id", js.get ( "id" ) );
							new_js.put ( "created_at", js.get ( "created_at" ) );
							new_js.put ( "geo", js.get ( "geo" ) );
							count--;
							// 文件代号计算
							if ( count == 0 )
							{
								num++;
								count = 50000;
							}
							writeFile ( ( path+num+".txt" ), new_js.toString ( )+ "\r\n" );		
						}
					}
				}
			}
		}
		catch ( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace ( );
		}
	}

	public static String segAnalyzer(String text)
	{
		String after_seg = "";
		try
		{
			Analyzer anal = new IKAnalyzer ( true );
			StringReader reader = new StringReader ( text );
			// 分词
			TokenStream ts = anal.tokenStream ( "", reader );
			CharTermAttribute term = ts
					.getAttribute ( CharTermAttribute.class );
			// 遍历分词数据
			while ( ts.incrementToken ( ) )
			{
				after_seg += term.toString ( ) + "|";
			}
			reader.close ( );
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return after_seg;
	}
	
	public static void writeFile ( String fileName, String content )
	{
		try
		{
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter ( fileName, true );
			writer.write ( content );
			writer.close ( );
		}
		catch ( IOException e )
		{
			e.printStackTrace ( );
		}
	}

	/**
	 * BUG文本处理
	 * 采用智能分词，而keyword为 数字+数字单位（十、百、千、万、亿），就会报错。
	 */
	private static String handBUG(String text)
	{
		if(text.contains ( "万" ) || text.contains ( "万" ) || text.contains ( "万" ) || text.contains ( "万" ) )
		{
			return text.replace (  "5万","50000" );
		}
		return text;
	}
	
	/**
	 * 文本处理 去掉末尾的位置url
	 */
	private static String handlehttp ( String text )
	{
		if ( text.indexOf ( "我在" + ":" ) != -1 )
		{
			int be = text.indexOf ( "我在" + ":" );
			int end = text.length ( );
			text = text.substring ( 0, be );
			// System.out.println("@处理http之后的text输出:"+text);
		}
		else if ( text.indexOf ( "我在这里" + ":" ) != -1 )
		{
			int be = text.indexOf ( "我在这里" + ":" );
			int end = text.length ( );
			text = text.substring ( 0, be );
			// System.out.println("@处理http之后的text输出:"+text);
		}
		return text;
	}

	/**
	 * 文本处理 去掉表情
	 */
	private static String handleemo ( String text )
	{
		Pattern p = Pattern.compile ( "\\[(.*?)\\]" );
		Matcher m = p.matcher ( text );
		while ( m.find ( ) )
		{
			// System.out.println(m.group(1));
			text = text.replaceAll ( "\\[(.*?)\\]", "" );
		}
		// System.out.println("@处理表情符号之后的text输出:"+text);
		return text;
	}

	/**
	 * 文本处理 去掉网址和链接
	 */
	private static String handleurl ( String text )
	{
		text = text.replaceAll ( "(?is)(?<!')(http://[/\\.\\w]+)", "" );
		// System.out.println("@处理网址之后的text输出:"+text);
		return text;
	}

	/**
	 * 文本处理 去掉关注人
	 */
	private static String handleat ( String text )
	{
		Pattern p = Pattern.compile ( "@(.*?) " );
		Matcher m = p.matcher ( text );
		while ( m.find ( ) )
		{
			text = text.replaceAll ( "@(.*?) ", "" );
			// System.out.println("@处理关注的人之后的text输出:"+text);
		}
		return text;
	}
}
