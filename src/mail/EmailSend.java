
package mail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import json.JSONException;
import json.JSONObject;
import stor.OperMongo;

public class EmailSend
{

	private MailSenderInfo	mailInfo	= null;

	/**
	 * 初始化设置发送邮箱（媒介邮箱）。
	 */
	public EmailSend ( )
	{
		// 这个是设置邮件
		mailInfo = new MailSenderInfo ( );
		mailInfo.setMailServerHost ( "smtp.163.com" );
		mailInfo.setMailServerPort ( "25" );// qq port
		mailInfo.setValidate ( true );
		mailInfo.setUserName ( "cug_gis_436_bdg@163.com" );
		mailInfo.setPassword ( "ABC1234567890abc" );// 您的邮箱密码
		mailInfo.setFromAddress ( "cug_gis_436_bdg@163.com" );
	}
	/**
	 * @author coco1
	 * 粗糙地简化一下邮件发送的函数
	 * @param emailad
	 * @param df
	 * @param sdf
	 * @param grab_statistic
	 * @param c
	 * @param email_send
	 * @throws JSONException
	 */
public void EmailSendByAddress(String emailad ,
							   SimpleDateFormat df ,
							   SimpleDateFormat sdf ,
							   JSONObject grab_statistic ,								
							   Calendar c ,
							   EmailSend email_send
								) throws JSONException
{

	email_send.send(emailad,
			"NO.1:BigDataGrab项目后台状态信息",
			"--------------------------------------------------------------------------------------"+"\r\n"+
			"[admin@~]邮件发送时间："+df.format ( new Date ( )) +"\r\n\r\n" +
			"[admin@~]正在获取:" + sdf.format ( c.getTime ( ) ) + "的数据。" +"\r\n" +
			"--------------------------------------------------------------------------------------"+"\r\n"+
			"[admin@~]数据抓取信息统计:"+"\r\n\r\n" +
			"    >----抓取次数:"+ grab_statistic.getInt ( "grab_num" ) +"次。\r\n\r\n" +
			"    >----抓取出错次数"+ grab_statistic.getInt ( "grab_error_num" ) +"次。\r\n\r\n" +
			"    >----抓取空次数"+ grab_statistic.getInt ( "grab_blank_num" ) +"次。\r\n\r\n" +
			"[admin@~]数据库信息统计:" +"\r\n\r\n" +
			"    >----数据库数目"+ OperMongo.getDBNum ( ) +"个。\r\n\r\n" +
			"    >----数据集数目"+ OperMongo.getCollectionNum ( ) +"个。\r\n\r\n" +
			"    >----数据记录总量"+ OperMongo.getDataNum ( ) +"条。\r\n"+
			"--------------------------------------------------------------------------------------"+"\r\n");
}
/**
 * @author coco1
 * 粗糙简化一下消耗时间
 * @param time
 * @param emailad
 * @param email_send
 */
public void EmailSendByAddress(long time,String emailad, EmailSend email_send)
{
	email_send.send(emailad,
			"BigDataGrab项目数据去重信息",
			"[admin@~]NO.1去重共耗时:"+time +"ms\r\n\r\n" +
			"[admin@~]接下来进行：行政区划归类\r\n\r\n");
	}
/**
 * @author coco1
 * 粗糙简化一下消耗时间
 * @param time
 * @param emailad
 * @param email_send
 */
public void EmailSendByAddress(long time,long time2,String emailad, EmailSend email_send,double blank_get,int blank_grab)
{
	email_send.send(emailad,
			"BigDataGrab项目数据去重信息",
			"[admin@~]NO.1行政区划归类共耗时:"+time +"s\r\n\r\n" +
			"[admin@~]完成一天的抓取"+
			"[admin@~]一共使用" + time2 +"min。\r\n"+
			"[admin@~]空抓后抓取成功次数/空抓次数为" + blank_get +"/"+ blank_grab +"。\r\n");
	
	}
/**
 * @author coco1
 * 粗糙简化一下当前状态
 * @param emailad
 * @param sdf
 * @param c
 * @param email_send
 */

public void EmailSendByAddress(String emailad ,
		   SimpleDateFormat sdf ,							
		   Calendar c ,
		   EmailSend email_send)
{
	email_send.send(emailad,
		"NO.1:BigDataGrab项目去重后统计信息",
		"--------------------------------------------------------------------------------------"+"\r\n"+
		"[admin@~]正在获取:" + sdf.format ( c.getTime ( ) ) + "的数据。" +"\r\n" +
		"--------------------------------------------------------------------------------------"+"\r\n"+
		"[admin@~]数据库信息统计:" +"\r\n\r\n" +
		"    >----数据库数目"+ OperMongo.getDBNum ( ) +"个。\r\n\r\n" +
		"    >----数据集数目"+ OperMongo.getCollectionNum ( ) +"个。\r\n\r\n" +
		"    >----数据记录总量"+ OperMongo.getDataNum ( ) +"条。\r\n"+
		"--------------------------------------------------------------------------------------"+"\r\n");
	}
	/**
	 * 
	 * 这个函数使用来发送邮件
	 * 
	 * @param to_address
	 * 
	 * @param subject
	 * 
	 * @param content
	 * 
	 */
	public void send ( String to_address, String subject, String content )
	{
		mailInfo.setToAddress ( to_address );
		mailInfo.setSubject ( subject );
		mailInfo.setContent ( content );
		SimpleMailSender sms = new SimpleMailSender ( );
		sms.sendTextMail ( mailInfo );// 发送文体格式
	}

	/**
	 * 这个函数使用来发送邮件
	 * 
	 * @param to_address
	 * 
	 * @param subject
	 * 
	 * @param content
	 * 
	 */
	public void send ( String to_address1, String to_address2, String subject,
			String content )
	{
		mailInfo.setToAddress ( to_address1 );
		mailInfo.setSubject ( subject );
		mailInfo.setContent ( content );
		SimpleMailSender sms = new SimpleMailSender ( );
		sms.sendTextMail ( mailInfo );// 发送文体格式
		mailInfo.setToAddress ( to_address2 );
		sms.sendTextMail ( mailInfo );// 发送文体格式
	}

	/**
	 * 这个函数使用来发送邮件
	 * 
	 * @param to_address
	 * 
	 * @param subject
	 * 
	 * @param content
	 */
	public void send ( String [ ] to_address, String subject, String content )
	{
		SimpleMailSender sms = new SimpleMailSender ( );
		mailInfo.setSubject ( subject );
		mailInfo.setContent ( content );
		for ( int i = 0; i < to_address.length; i++ )
		{
			mailInfo.setToAddress ( to_address[i] );
			sms.sendTextMail ( mailInfo );// 发送文体格式
		}
	}
}
/*
email_send.send(email_addresses,
						"BigDataGrab项目后台状态信息",
						"--------------------------------------------------------------------------------------"+"\r\n"+
						"[admin@~]邮件发送时间："+df.format ( new Date ( )) +"\r\n\r\n" +
						"[admin@~]正在获取:" + sdf.format ( c.getTime ( ) ) + "的数据。" +"\r\n" +
						"--------------------------------------------------------------------------------------"+"\r\n"+
						"[admin@~]数据抓取信息统计:"+"\r\n\r\n" +
						"    >----抓取次数:"+ grab_statistic.getInt ( "grab_num" ) +"次。\r\n\r\n" +
						"    >----抓取出错次数"+ grab_statistic.getInt ( "grab_error_num" ) +"次。\r\n\r\n" +
						"    >----抓取空次数"+ grab_statistic.getInt ( "grab_blank_num" ) +"次。\r\n\r\n" +
						"[admin@~]数据库信息统计:" +"\r\n\r\n" +
						"    >----数据库数目"+ OperMongo.getDBNum ( ) +"个。\r\n\r\n" +
						"    >----数据集数目"+ OperMongo.getCollectionNum ( ) +"个。\r\n\r\n" +
						"    >----数据记录总量"+ OperMongo.getDataNum ( ) +"条。\r\n"+
						"--------------------------------------------------------------------------------------"+"\r\n");
*/
