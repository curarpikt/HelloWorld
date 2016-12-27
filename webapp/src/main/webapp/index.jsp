<!-- this is unprotected url -->
<%@ page import="com.chanjet.csp.ccs.api.common.EnterpriseContext" %>
<%@ page import="com.chanjet.csp.appmanager.AppWorkManager" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chanjet Welcome</title>
</head>
<script type="text/javascript"  src="http://static.chanapp.chanjet.com/third-lib/dojo/1.9.6/dojo/dojo.js"></script>
<script type="text/javascript">
require(["dojo/request/script",
         "dojo/request",
         "dojo/dom",
         "dojo/html",
         "dojo/domReady!"], function(script, request, dom, html){
	
	//检查CIA是否登录的URL
	var cia_url = "http://cia.chanapp.chanjet.com/internal_api/authorizeByJsonp?client_id=<%=EnterpriseContext.getClientId()%>&state=xxsss";
	//检查应用是否能够经过权限检查的url
	var check_login_url = "services/1.0/user/session";
	//检查登录成功后，转到的工作页面
	var welcome_url="welcome.jsp";

	script.get(cia_url, {
		jsonp: "callback"
	}).then(function(result){
		console.log(result);
		if (result.code) {
			//login at cia,now check login app
			request(check_login_url+"?code="+result.code).then( 
				function(data){
					// data - response from server
					console.log(data);
					html.set(dom.byId("info"), "检查用户登录成功，正在转移首页...");
					window.location.href = welcome_url;
				},function(error){
					console.log(error);
					switch(error.response.status){
					case 403:
						html.set(dom.byId("info"), "检测到用户已登录，但不是本企业员工，或者没有权限使用本应用。请按下面登录址重新登录后再回到本页面刷新。");
						break;
					default:
						html.set(dom.byId("info"), "检测到用户已登录，但应用登录检查失败，请刷新页面重试， 或者按下面登录址重新登录后再回到本页面刷新。");
					}
				});
		}else{
			//not login at cia
			html.set(dom.byId("info"), "没有检查到登录用户，请按下面登录址登录后再回到本页面刷新。");
		}
	}, function(err){
		console.log(err);
	});
});
</script>
<body>
<h1> 欢迎来到畅捷通 </h1>

<h2 id="info">正在检查登录用户...， 如果页面长时间无反应，请按下面登录址登录后再回到本页面。</h2>

登录地址： <a href="http://www.chanjet.com/login?app=<%=AppWorkManager.getCurrentAppId()%>&logout=1" target="_blank">畅捷通 官方网站</a> 

</body>
</html>
