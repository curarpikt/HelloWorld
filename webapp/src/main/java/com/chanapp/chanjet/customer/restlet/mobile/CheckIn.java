package com.chanapp.chanjet.customer.restlet.mobile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;

public class CheckIn extends BaseRestlet{

	@Override
	public Object run() {	
	 if(this.getMethod().equals(MethodEnum.POST)){// 新增联系人
			String payload =this.getPayload();
			return addCheckIn(payload);
		}else  if(this.getMethod().equals(MethodEnum.DELETE)){// 新增联系人
			Long checkinId = this.getId();
			return deleleCheckIn(checkinId);
		}else  if(this.getMethod().equals(MethodEnum.GET)){// 新增联系人
			Long checkinId = this.getId();
			if(checkinId!=null){
				return getCheckinDetail(checkinId);
			}else{
				Integer first = this.getParamAsInt("first");
				Integer max = this.getParamAsInt("max");
				Assert.notNull(first, "app.common.para.format.error");
				Assert.notNull(max, "app.common.para.format.error");
				Map<String,Object> para = new HashMap<String,Object>();
				para.put("owner", this.getParam("owner"));				
				para.put("keyWord", this.getParam("keyWord"));
				if(this.getParam("customerId")!=null){
					para.put("customerId",  this.getParamAsLong("customerId"));	
				}					
				return getCheckinList(first,max,para);
			}
		}
		

	    return null;
   }
	 /**
	  * @api {GET} /restlet/mobile/CheckIn/{Id} 2.08  客户拜访详情页面 
	  * @apiName checkInDetail
	  * @apiGroup H5ITF 0.2
	  * @apiVersion 0.0.0
	  * @apiDescription  客户拜访编辑页面先查询详情
	  * @apiSuccess {Long} lastModifiedDate 最后修改时间
	  * @apiSuccess {String} remark 拜访正文
	  * @apiSuccess {Long} customerId 客户ID
	  * @apiSuccess {String} customerName 客户名称
	  * @apiSuccess {Object} coordinate 坐标
	  * @apiSuccess {Long}   privilege 权限字段 (1 只读,5读删)
	  * @apiSuccess {Object} coordinateNote 坐标地址
	  * @apiSuccess {Object} attachments 附件对象数组
	  * @apiSuccess {String =file,image,audio} attachments.category 附件类型
	  * @apiSuccess {String} attachments.fileDir 附件云存储地址
	  * @apiSuccess {String} attachments.fileName 附件文件名
	  * @apiSuccess {String} attachments.fileType 附件文件后缀
	  * @apiSuccess {String} attachments.size 附件大小
	  * @apiSuccess {String} attachments.extend 附件扩展属性 eg:"{"height":200,"width":100,"duration":1}"
	  * @apiSuccessExample {json} 返回结果举例: 
			{
			    "customerName": "customer02", 
			    "id": 100002, 
			    "customerId": 100002, 
			    "coordinate": {
			        "longitude": 116.236256, 
			        "latitude": 40.067462
			    }, 
			    "remark": "testcheckin", 
			    "lastModifiedDate": 1462252545357, 
			    "coordinateNote": "用友软件园中区9D", 
			    "attachments": [
			        {
			            "fileType": "jpg", 
			            "category": "image", 
			            "fileDir": "http://sto.chanapp.chanjet.com/90003746214/2016/04/29/03e04a22c85947e1a54e0075f233495b.jpg", 
			            "extend": null, 
			            "fileName": "tmp_1461895442243.jpg", 
			            "size": 20097
			        }
			    ]
			}
	  * @apiErrorExample {json} Error-Response: 
	  *  HTTP/1.1 400 BaseException
	  */
		public String getCheckinDetail(Long id){	
			CheckinServiceItf checkInService = ServiceLocator.getInstance().lookup(CheckinServiceItf.class);
			return checkInService.getCheckinDetail(id);
		}
		
		 /**
		  * @api {GET} /restlet/mobile/CheckIn 2.09  客户拜访列表 
		  * @apiName checkInList
		  * @apiGroup H5ITF 0.2
		  * @apiVersion 0.0.0
		  * @apiDescription  客户拜访列表页面
		  * @apiParam {int} first 开始条数
		  * @apiParam {int} max 每页条数
		  * @apiParam {String} [keyWord] 查询关键词(客户名称，联系人名称，电话)
		  * @apiParam {Long} [customerId] 客户ID
		  * @apiParam {String} [owner] 业务员ID数组  eg:100003,100005
		  * @apiSuccess {Boolean} hasMore 是否还有数据
		  * @apiSuccess {Object} items 列表数据数组
	      * @apiSuccess {Long} items.lastModifiedDate 最后修改时间
		  * @apiSuccess {String} items.remark 拜访正文
		  * @apiSuccess {Long} items.customerId 客户ID
		  * @apiSuccess {Long} items.privilege 权限字段 (1 只读,5读删)
		  * @apiSuccess {String} items.customerName 客户名称
		  * @apiSuccess {Object} items.coordinate 坐标
		  * @apiSuccess {Object} items.coordinateNote 坐标地址
		  * @apiSuccess {Object} items.attachments 附件对象数组
		  * @apiSuccess {String =file,image,audio} items.attachments.category 附件类型
		  * @apiSuccess {String} items.attachments.fileDir 附件云存储地址
		  * @apiSuccess {String} items.attachments.fileName 附件文件名
		  * @apiSuccess {String} items.attachments.fileType 附件文件后缀
		  * @apiSuccess {String} items.attachments.size 附件大小
		  * @apiSuccess {String} items.attachments.extend 附件扩展属性 eg:"{"height":200,"width":100,"duration":1}"
		  * @apiSuccessExample 返回结果举例: 
		  * {
		  * "hasMore":true,
			  "items":[{
			  	"lastModifiedDate":19899823323,
			    "remark":"",
				"customerId":"",
				"customerName":"",
				"coordinate":"",
				"coordinateNote":"",
			    "owner":{
			      "headPicture":"",
			      "name":"",
			      "id":
			    },
			    "attachments":[{
			      "category":"",
			      "fileDir":"",
			      "fileName":"",
			      "fileType":"",
				  "extend":"",
			      "size":"845941",//历史数据
			      "id":111
			      }]
			  }]
		  * }
		  * @apiErrorExample {json} Error-Response: 
		  *  HTTP/1.1 400 BaseException
		  */
		
		public String getCheckinList(Integer first,Integer max,Map<String,Object> para){
			CheckinServiceItf checkInService = ServiceLocator.getInstance().lookup(CheckinServiceItf.class);
			return checkInService.getCheckinList(first, max, para);
		}
		
		 /**
		 * @api {DELETE} /restlet/mobile/CheckIn/{Id} 2.10 删除拜访记录 
	     * @apiName deleteCheckIn
	     * @apiGroup H5ITF 0.2
	     * @apiVersion 0.0.0
	     * @apiDescription 根据拜访记录ID，逻辑删除拜访记录。
	     * @apiSuccess {String} 200 结果码
	     * @apiErrorExample {json} Error-Response: 
	     *  HTTP/1.1 400 BaseException
	     *  {"code":"10322","message":"app.checkIn.object.notexist","info":"拜访记录不存在"}
	     */

		public String deleleCheckIn(Long checkinId){
			ProcessResult processResult = new ProcessResult(true);
			CheckinServiceItf checkInService = ServiceLocator.getInstance().lookup(CheckinServiceItf.class);
			checkInService.deleteCheckin(checkinId);
			return AppWorkManager.getDataManager().toJSONString(processResult);
		}
		
		
		 /**
		* @api {POST} /restlet/mobile/CheckIn 2.12  新建拜访记录 
	    * @apiName addCheckIn
	    * @apiGroup H5ITF 0.2
	    * @apiVersion 0.0.0
	    * @apiDescription  新建拜访记录(包含附件数组)
	    * @apiParam {string}  remark "拜访文本"
	    * @apiParam {string}  [customerId] "客户ID"
	    * @apiParam {string}  [coordinate] "坐标"
	    * @apiParam {string}  [coordinateNote] "坐标地址"   
	    * @apiParam {Object}  [attachments] 附件信息
	    * @apiParam {String =file,image,audio} [attachments.category] 附件类型 
	    * @apiParam {String} [attachments.fileDir] 附件云存储地址
		* @apiParam {String} [attachments.fileName] 附件文件名
		* @apiParam {String} [attachments.fileType] 附件文件后缀
		* @apiParam {Integer} [attachments.size] 附件大小
		* @apiParam {Object} [attachments.extend] 附件扩展属性 eg:"{"height":200,"width":100,"duration":1}"	
	    * @apiParamExample {json} 举例:
			{
			    "customerName": "22222",
			    "customerId": 100002,
			    "remark": {
			        "longitude": 116.236256,
			        "latitude": 40.067462
			    },
			    "coordinate": {
			        "longitude": 116.236256,
			        "latitude": 40.067462
			    },
			    "privilege": 5,
			    "lastModifiedDate": 1461910467075,
			    "coordinateNote": "用友软件园中区9D",
			    "attachments": [
			        {
			            "fileType": "jpg",
			            "category": "image",
			            "fileDir": "http://sto.chanapp.chanjet.com/90003746214/2016/04/29/03e04a22c85947e1a54e0075f233495b.jpg",
			            "extend": null,
			            "fileName": "tmp_1461895442243.jpg",
			            "size": 20097
			        }
			    ]
			}
	    * 
	  * @apiSuccess {Long} lastModifiedDate 最后修改时间
	  * @apiSuccess {String} remark 拜访正文
	  * @apiSuccess {Long} customerId 客户ID
	  * @apiSuccess {Long}   privilege 权限字段 (1 只读,5读删)
	  * @apiSuccess {String} customerName 客户名称
	  * @apiSuccess {Object} coordinate 坐标
	  * @apiSuccess {Object} coordinateNote 坐标地址
	  * @apiSuccess {Integer} privilege 操作权限
	  * @apiSuccess {Object} attachments 附件对象数组
	  * @apiSuccess {String =file,image,audio} attachments.category 附件类型
	  * @apiSuccess {String} attachments.fileDir 附件云存储地址
	  * @apiSuccess {String} attachments.fileName 附件文件名
	  * @apiSuccess {String} attachments.fileType 附件文件后缀
	  * @apiSuccess {String} attachments.size 附件大小
	  * @apiSuccess {String} attachments.extend 附件扩展属性 eg:"{"height":200,"width":100,"duration":1}"
	  * @apiSuccessExample {json} 返回结果举例: 
		 {
		    "lastModifiedDate":19899823323,
		    "remark":"",
			"customerId":"",
			"customerName":"",
			"coordinate":"",
			"coordinateNote":"",
		    "attachments":[{
		      "category":"",
		      "fileDir":"",
		      "fileName":"",
		      "fileType":"",
			  "extend":"",
		      "size":"845941",//历史数据
		      "id":111
		      }]
		  }
	    * @apiErrorExample {json} Error-Response: 
	    *  HTTP/1.1 400 BaseException
	    */
		
		public String addCheckIn(String payload){
			CheckinServiceItf checkInService = ServiceLocator.getInstance().lookup(CheckinServiceItf.class);
			return checkInService.addCheckInForH5(payload);
		}
}
