package method;
/**
 * 
 * @author Shaow
 * 目的是创建一个存储单次抓取的链表
 * @param lat 存储此次抓取的纬度
 * @param lng 存储此次抓取的经度
 * @param pointType 存储抓取点的类型（是否上次抓到）
 * @param numbers 存储这个点上累计获取的数量
 * @param lastgrab 存储这个点上本次抓取的数量（或者说是上次）
 * @param city
 * @param province
 * 
 */
public class point {
				private String province ;
				private String city ;
				/**
				 * 
				 * @param lat
				 * @param lng
				 * @param pointType
				 * @param lastgrab
				 * @param num
				 */
				public point(String province,String city )
				{
					this.city = city;
					this.province = province;
				}
				public String getProvince() {
					return province;
				}
				public void setProvince(String province) {
					this.province = province;
				}
				public String getCity() {
					return city;
				}
				public void setCity(String city) {
					this.city = city;
				}
				
}
