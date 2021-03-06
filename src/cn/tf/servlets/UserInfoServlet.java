package cn.tf.servlets;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.RandomStringUtils;

import cn.tf.biz.IAdminInfoBiz;
import cn.tf.biz.UserInfoBiz;
import cn.tf.biz.impl.AdminInfoBizImpl;
import cn.tf.biz.impl.UserInfoBizImpl;
import cn.tf.dao.UserInfoDao;
import cn.tf.dao.impl.UserInfoDaoImpl;
import cn.tf.entities.AdminInfo;
import cn.tf.entities.UserInfo;
import cn.tf.utils.AttributeData;
import cn.tf.utils.SendMailThread;
import cn.tf.utils.SendMailThread1;
import cn.tf.utils.UploadUtil;
import cn.tf.utils.WebUtil;


@WebServlet(name="UserInfoServlet",urlPatterns="/servlet/UserInfoServlet")
public class UserInfoServlet extends BasicServlet {

   

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String op=request.getParameter("op");
		
		if("sendEmail".equals(op)){
			sendEmail(request,response);
		}else if("chenkoutDate".equals(op)){
			chenkoutDate(request,response);
		}else if("registUser".equals(op)){
			registUser(request,response);
		}else if("userLogin".equals(op)){
			userLogin(request,response);
		}else if("getLoginInfo".equals(op)){
			getLoginInfo(request,response);
		}else if("findUserInfoByPage".equals(op)){
			findUserInfoByPage(request,response);
		}else if("deleteUserInfo".equals(op)){
			deleteUserInfo(request,response);
		}else if("searchUserInfoByPage".equals(op)){
			searchUserInfoByPage(request,response);
		}else if("loginout".equals(op)){
			loginout(request,response);
		}
		

	}


	//退出登录
	private void loginout(HttpServletRequest request,
			HttpServletResponse response) {
		
		request.getSession().removeAttribute(AttributeData.CURRENTUSERLOGIN);
		this.out(response, 1);
	}


	//条件查询
	private void searchUserInfoByPage(HttpServletRequest request,
			HttpServletResponse response) {
		String prov=request.getParameter("prov");
		String city=request.getParameter("city");
		String area=request.getParameter("area");	
		String uname=request.getParameter("uname");
		String status=request.getParameter("status");
		
		
		String pageNo=request.getParameter("page");
		String pageSize=request.getParameter("rows");
		
		Map<String,String>  param=new HashMap<String,String>();
		
		if(!"--请选择省份--".equals(prov)){
			param.put("  prov=", prov);
		}
		if(!"--请选择城市--".equals(city)){
			param.put("  city=", city);
		}
		if(!"--请选择地区--".equals(area)){
			param.put("  area=", area);
		}
		
		if(!"-1".equals(status)){
			param.put(" status=", status);
		}
		
		if(uname!=null && !"".equals(uname)){
			param.put(" uname like ", "%"+uname+"%");
		}
		UserInfoBiz userInfoBiz=new UserInfoBizImpl();
		List<UserInfo>  list=userInfoBiz.find(param,Integer.parseInt(pageNo), Integer.parseInt(pageSize));
		List<UserInfo>  list1=userInfoBiz.find(param,null,null);
		this.out(response, list,list1.size());
		
		
		
	}



	//冻结解冻客户
	private void deleteUserInfo(HttpServletRequest request,
			HttpServletResponse response) {
		String value=request.getParameter("value");
		String usid=request.getParameter("usid");
		UserInfoBiz userInfoBiz=new UserInfoBizImpl();
		
		int result=userInfoBiz.del(usid,value);
		if(result>0){
			this.getServletContext().getAttribute(AttributeData.CURRENTUSERLOGIN);
		}

		this.out(response,result);
		
		
		
	}


	//查询所有用户信息
	private void findUserInfoByPage(HttpServletRequest request,
			HttpServletResponse response) {
		String pageNo=request.getParameter("page");
		String pageSize=request.getParameter("rows");
		
		UserInfoBiz userInfoBiz=new UserInfoBizImpl();
		List<UserInfo>  list=userInfoBiz.find(Integer.parseInt(pageNo),Integer.parseInt(pageSize));
		
		this.out(response, list,userInfoBiz.getTotal(null));
		
	}


	//获取用户信息
	private void getLoginInfo(HttpServletRequest request,
			HttpServletResponse response) {
		HttpSession session=request.getSession();
		Object obj=session.getAttribute(AttributeData.CURRENTUSERLOGIN);
		if(obj==null){
			this.out(response,obj);
		}else{
			this.out(response, (UserInfo)obj );
		}
		
	}


	//用户登录
	private void userLogin(HttpServletRequest request,
			HttpServletResponse response) {
		
		String name=request.getParameter("name");
		String pwd=request.getParameter("pwd");
		String code=request.getParameter("code");
		
		HttpSession session=request.getSession();
		String codes=(String) session.getAttribute("rand");
		int result=0;
		
		if(codes.equals(code)){
			//验证码输入正确，则调用业务层
			UserInfoBiz userInfoBiz=new UserInfoBizImpl();
			UserInfo userInfo=userInfoBiz.login(name, pwd);
			if(userInfo==null){
				result=2;   //用户名或密码错误
			}else{
				result=3; //登录成功
				
				//将当前登录用户存起来，以便后面的页面使用当前用户信息
				session.setAttribute(AttributeData.CURRENTUSERLOGIN, userInfo);
			}	
			
		}else{
			result=1;  //验证码错误
		}
		this.out(response, result);
		
		
	}


	//完成注册
	private void registUser(HttpServletRequest request,
			HttpServletResponse response) {
		
		String username=request.getParameter("username");
		String pwd=request.getParameter("pwd");
		String tel=request.getParameter("tel");
		String email=request.getParameter("email");
		String prov=request.getParameter("prov");
		String city=request.getParameter("city");
		String area=request.getParameter("area");
		
		UserInfoBiz userInfoBiz=new UserInfoBizImpl();
		int result=userInfoBiz.add(username,pwd,tel,email,prov,city,area);
		
		this.out(response, result);
		
	}


	//验证邮箱验证码是否正确
	private void chenkoutDate(HttpServletRequest request,
			HttpServletResponse response) {
		
		String rcode=request.getParameter("rcode");
		String code=(String) request.getSession().getAttribute("code");	

		if(code==null){
			this.out(response, 2);
		}else  if(rcode.equals(code))	{
			this.out(response, 1);
		}else{
			this.out(response, 0);
		}
		
		
	}


	//注册发送邮件
	private void sendEmail(HttpServletRequest request,
			HttpServletResponse response) {
		UserInfo userInfo=WebUtil.fillBean(request,UserInfo.class);

		String code =RandomStringUtils.randomNumeric(5);

		userInfo.setCode(code);
		
		HttpSession session=request.getSession();
		session.setAttribute("code", code);
		//300秒后超时
		Timer timer=new Timer();
		timer.schedule(new TimerTask(){
			public void run(){
				session.removeAttribute("code");
			}
		}, 300000);

		SendMailThread1 smt=new SendMailThread1(userInfo);
		smt.start();
		this.out(response, 1);
		
	}
	
	
	
	

}
