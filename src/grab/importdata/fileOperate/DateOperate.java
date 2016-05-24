package grab.importdata.fileOperate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 日期操作方法
 * @author licong
 *
 */
public class DateOperate {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
     * 取得起止日期间的日期集合
     * @param startDate
     * @param endDate
     * @return 日期list(包含startDate,不包含endDate)
     */
    public static List<String> getDateList(String startDate, String endDate){
    	List<String> al = new ArrayList<String>();
    	if(startDate.equals(endDate)){
    		al.add(startDate);
    		}
    	else if(startDate.compareTo(endDate) < 0){
    		while(startDate.compareTo(endDate) < 0){
    			al.add(startDate);
    			try {
    				Long l = format.parse(startDate).getTime();
    				startDate = format.format( l + 3600*24*1000);//+1天
    				} catch (Exception e) {
    					e.printStackTrace();
    					}
    			}
    		}
    	else{
    		al.add(startDate);
    		}
    	return al;
    	}
    
	/**
	 * 判断当前日期是星期几
	 * @param time 修要判断的时间 yyyy-MM-dd HH:mm:ss
	 * @return dayForWeek
	 * @Exception 
	 */
    public static int dayForWeek(String time) throws Exception {
    	Calendar c = Calendar.getInstance();c.setTime(format1.parse(time));
    	int dayForWeek = 0;
    	if(c.get(Calendar.DAY_OF_WEEK) == 1){
    		dayForWeek = 7;
    		}else{
    			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
    			}
    	return dayForWeek;
    	}
}
