package com.yixi.pkg.app;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
//import java.sql.Types;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
public class AppHandler implements HttpHandler{
	public Jedis jedis=null;
	public ComboPooledDataSource cpds=null;
	public AppHandler(Jedis j,ComboPooledDataSource c){
		jedis=j;
		cpds=c;
	}
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// TODO Auto-generated method stub
		String resquestMethod=exchange.getRequestMethod();
		Headers header=exchange.getResponseHeaders();
		OutputStream outResponseBody=exchange.getResponseBody();
		URI uri=exchange.getRequestURI();
		String path=uri.getPath();
		System.out.println(path);
		header.set("Content-type", "application/json; charset=utf-8");
		header.set("Access-Control-Allow-Origin", "*");
		header.set("Access-Control-Allow-Headers", "x-requested-with,content-type");
		byte[] writeResult = null;
		JSONObject jsonResult=new JSONObject();
		if(!resquestMethod.equalsIgnoreCase("post"))
		{
			jsonResult.put("result", "1");
			jsonResult.put("message", "��ʹ��post����");
		}
		else
		{
			byte[] readRseult=new byte[1024];
			InputStream inRequestBody=exchange.getRequestBody();
			inRequestBody.read(readRseult);
			JSONObject jsonBody=null;
			try {
				String requestBody=new String(readRseult,"utf-8");
				jsonBody=new JSONObject(requestBody);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				jsonResult.put("result", "1");
				jsonResult.put("message", "�����������");
				e.printStackTrace();
			}
			if(jsonBody!=null){
				ResultInfo rs=null;
				// ����ͷ�
				if(path.equalsIgnoreCase("/Cus_Key_Get"))
				{
					jsonResult.put("result", "0");
					jsonResult.put("message", "�ɹ�");
					jsonResult.put("AppKey", "de4ec8c06b8ea74ff6cc017ca1116ebd");
					jsonResult.put("Appname", "���ڱ�");
				}
				else{
					// ���App��
					if(path.equalsIgnoreCase("/app_manage_add")){
						rs=addAppInfo(jsonBody);
					}
					// ɾ��app��
					if(path.equalsIgnoreCase("/app_manage_remove")){
						rs=removeAppInfo(jsonBody);
					}
					// �޸�app��
					if(path.equalsIgnoreCase("/app_manage_modify")){
						rs=modifyAppInfo(jsonBody);
					}
					// �鿴app������
					if(path.equalsIgnoreCase("/app_manage_info")){
						rs=getAppInfo(jsonBody);
					}
					// ��ȡapp���б�
					if(path.equalsIgnoreCase("/app_manage_list")){
						rs=getAppList(jsonBody);
					}
					// ���汾����
					if(path.equalsIgnoreCase("/app_manage_checkUpdate")){
						rs=checkUpdateApp(jsonBody);
					}
					// ��ȡ���ذ�url
					if(path.equalsIgnoreCase("/app_manage_download")){
						rs=downloadApp(jsonBody);
					}
					// �Ƿ����չʾ
					if(path.equalsIgnoreCase("/app_manage_isdisplay")){
						rs=getDisplay(jsonBody);
					}
					// ��ӹ���
					if(path.equalsIgnoreCase("/notice_manage_add")){
						rs=addNoticeInfo(jsonBody);
					}
					// ɾ������
					if(path.equalsIgnoreCase("/notice_manage_remove")){
						rs=removeNotice(jsonBody);
					}
					// �޸Ĺ���
					if(path.equalsIgnoreCase("/notice_manage_modify")){
						rs=modifyNotice(jsonBody);
					}
					// ��ȡ��������
					if(path.equalsIgnoreCase("/notice_manage_info")){
						rs=getNoticeInfo(jsonBody);
					}
					// ��ȡ�����б�
					if(path.equalsIgnoreCase("/notice_manage_list")){
						rs=getNoticeList(jsonBody);
					}
					// ��������
					if(path.equalsIgnoreCase("/notice_manage_publish")){
						rs=publishNotice(jsonBody);
					}
					// ��ȡ���¹���
					if(path.equalsIgnoreCase("/notice_manage_latest")){
						rs=getLatestNotice(jsonBody);
					}
					jsonResult.put("result", rs.state);
					jsonResult.put("message", rs.message);
					jsonResult.put("data", rs.data);
				}

			}
		}
		writeResult=jsonResult.toString().getBytes("utf-8");
		exchange.sendResponseHeaders(200, writeResult.length);
		outResponseBody.write(writeResult);
		outResponseBody.flush();
		outResponseBody.close();
	}
	// ����App��Ϣ
	public ResultInfo addAppInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String deviceType=json.getString("deviceType"); // �豸����
			String platformName=json.getString("platformName"); // ����
			String agentName=json.getString("agentName"); // ��������
			String appName=json.getString("appName");  // App����
		    String setupName=json.getString("setupName"); // ��װ������
		    String version=json.getString("version");  // �汾��
		    String internalVersion=json.getString("internalVersion"); //�ڲ��汾�� 
		    String appUrl=json.getString("appUrl"); // ��װ��·��
		    String startStatus=json.getString("startStatus"); // ����״̬
		    String forcedupdate=json.getString("forcedUpdate"); // ǿ�Ƹ��±�־
		    String notice=json.getString("notice"); // ����
		    String isDisplay=json.getString("isDisplay"); // ���չʾ��־
			String sessionKey=json.getString("cnckey"); 
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(deviceType.isEmpty())
			{
				res.state="1";
				res.message="�豸���Ͳ���Ϊ��";
				return res;
			}
			if(platformName.isEmpty())
			{
				res.state="1";
				res.message="��������Ϊ��";
				return res;
			}
			if(agentName.isEmpty())
			{
				res.state="1";
				res.message="���������Ʋ���Ϊ��";
				return res;
			}
			if(appName.isEmpty())
			{
				res.state="1";
				res.message="�汾���Ʋ���Ϊ��";
				return res;
			}
			if(setupName.isEmpty())
			{
				res.state="1";
				res.message="��װ�����Ʋ���Ϊ��";
				return res;
			}
			if(version.isEmpty())
			{
				res.state="1";
				res.message="�汾�Ų���Ϊ��";
				return res;
			}
			if(internalVersion.isEmpty())
			{
				res.state="1";
				res.message="�ڲ��汾�Ų���Ϊ��";
				return res;
			}
			if(appUrl.isEmpty())
			{
				res.state="1";
				res.message="app��·������Ϊ��";
				return res;
			}
			if(startStatus.isEmpty())
			{
				res.state="1";
				res.message="״̬�Ƿ����ò���Ϊ��";
				return res;
			}
			if(forcedupdate.isEmpty())
			{
				res.state="1";
				res.message="�Ƿ�ǿ�Ƹ��²���Ϊ��";
				return res;
			}
			if(isDisplay.isEmpty())
			{
				res.state="1";
				res.message="�Ƿ����չʾ����Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			int sqlResult=-1;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_add(?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setInt(1, Integer.parseInt(deviceType));
			cs.setString(2, platformName);
			cs.setString(3, agentName);
			cs.setString(4, appName);
			cs.setString(5, setupName);
			cs.setInt(6, Integer.parseInt(version));
			cs.setString(7, internalVersion);
			cs.setString(8, appUrl);
			cs.setInt(9, Integer.parseInt(startStatus));
			cs.setInt(10, Integer.parseInt(forcedupdate));
			cs.setString(11, notice);
			cs.setInt(12, Integer.parseInt(isDisplay));
			ResultSet rs=cs.executeQuery();
			while(rs.next())
			{
				sqlResult=rs.getInt("add_result");
			}
			if(sqlResult==1)
			{
				res.state="1";
				res.message="�İ�װ���Ѵ���";
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ɾ��App��Ϣ
	public ResultInfo removeAppInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try {
			String appID=json.getString("appID");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(appID.isEmpty())
			{
				res.state="1";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_remove(?)}");
			cs.setInt(1, Integer.parseInt(appID));
			ResultSet rs=cs.executeQuery();
			int sqlResult=0;
			while(rs.next()){
				sqlResult=rs.getInt("remove_result");
			}
			if(sqlResult==1){
				res.state="1";
				res.message="�ٷ�������ɾ��";
			}
			cs.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// �޸�App��Ϣ
	public ResultInfo modifyAppInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String appID=json.getString("appID");
			String deviceType=json.getString("deviceType");
			String platformName=json.getString("platformName");
			String agentName=json.getString("agentName");
			String appName=json.getString("appName");
		    String setupName=json.getString("setupName");
		    String version=json.getString("version");
		    String internalVersion=json.getString("internalVersion");
		    String appUrl=json.getString("appUrl");
		    String startStatus=json.getString("startStatus");
		    String forcedupdate=json.getString("forcedUpdate");
		    String notice=json.getString("notice");
		    String isDisplay=json.getString("isDisplay");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(appID.isEmpty())
			{
				res.state="5";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			int sqlResult=0;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_modify(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setInt(1, Integer.parseInt(appID));
			cs.setString(2, deviceType);
			cs.setString(3, platformName);
			cs.setString(4, agentName);
			cs.setString(5, appName);
			cs.setString(6, setupName);
			cs.setString(7, version);
			cs.setString(8, internalVersion);
			cs.setString(9, appUrl);
			cs.setString(10,startStatus);
			cs.setString(11,forcedupdate);
			cs.setString(12, notice);
			cs.setString(13,isDisplay);
			ResultSet rs=cs.executeQuery();
			while(rs.next())
			{
				sqlResult=rs.getInt("modify_result");
			}
			if(sqlResult==0)
			{
				CallableStatement cs1=conn.prepareCall("{call chain_futures_app.sp_app_info(?)}");
				cs1.setInt(1, Integer.parseInt(appID));
				ResultSet rs1=cs1.executeQuery();
				ResultSetMetaData rsmd=rs1.getMetaData();
				while(rs1.next())
				{
					for(int i=1;i<=rsmd.getColumnCount();i++)
					{
						columnLabel=rsmd.getColumnLabel(i);
						columnValue=rs1.getString(columnLabel);
						js.put(columnLabel, columnValue);
					}
				}
				rs1.close();
				cs1.close();
			}
			else if(sqlResult==1)
			{
				res.state="1";
				res.message="�İ�װ���Ѵ���";
			}
			else if(sqlResult==2)
			{
				res.state="1";
				res.message="�ٷ����������豸���ͣ���װ���������޸�";
			}
			else{
				res.state="1";
				res.message="��������,�޸�ʧ��";
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ��ȡapp��Ϣ����
	public ResultInfo getAppInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		res.data=js;
		Connection conn=null;
		try
		{
			String appID=json.getString("appID");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(appID.isEmpty())
			{
				res.state="1";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_info(?)}");
			cs.setInt(1, Integer.parseInt(appID));
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			while(rs.next())
			{
				for(int i=1;i<=rsmd.getColumnCount();i++)
				{
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ��ȡapp��Ϣ�б�
	public ResultInfo getAppList(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			int pageNum=json.getInt("pageNum");
			int pageSize=json.getInt("pageSize");
			String beginTime=json.getString("beginTime");
			String endTime=json.getString("endTime");
			String agentName=json.getString("agentName");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_list(?,?,?,?,?)}");
			cs.setInt(1, (pageNum-1)*pageSize);
			cs.setInt(2, pageSize);
			cs.setString(3, beginTime);
			cs.setString(4, endTime);
			cs.setString(5, agentName);
			cs.execute();
			ArrayList<JSONObject> list=new ArrayList<JSONObject>();
			int nResult=0;
			do
			{
				ResultSet rs=cs.getResultSet();
				ResultSetMetaData rsmd=rs.getMetaData();
				while(rs.next())
				{
					if(nResult==0)
					{
						for(int i=1;i<=rsmd.getColumnCount();i++)
						{
							columnLabel=rsmd.getColumnLabel(i);
							columnValue=rs.getString(columnLabel);
							js.put(columnLabel, columnValue);
						}
					}
					if(nResult==1)
					{
						JSONObject orderInfo=new JSONObject();
						for(int i=1;i<=rsmd.getColumnCount();i++)
						{
							columnLabel=rsmd.getColumnLabel(i);
							columnValue=rs.getString(columnLabel);
							orderInfo.put(columnLabel, columnValue);
						}
						list.add(orderInfo);
					}
				}
				nResult++;
				rs.close();
			}while(cs.getMoreResults());
			js.put("pageNum",pageNum);
			js.put("pageSize", pageSize);
			js.put("list", list);
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ������
	public ResultInfo checkUpdateApp(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String platformName=json.getString("platformName");
			String version=json.getString("version");
			String setupName=json.getString("setupName");
			String deviceType=json.getString("deviceType");
//			String sessionKey=json.getString("cnckey");
//			String sessionValue=jedis.get(sessionKey);
//			if(sessionValue==null)
//			{
//				res.state="5";
//				res.message="�˻�δ��¼";
//				return res;
//			}
			if(platformName.isEmpty())
			{
				res.state="1";
				res.message="�������Ʋ���Ϊ��";
				return res;
			}
			if(setupName.isEmpty())
			{
				res.state="1";
				res.message="��װ��������Ϊ��";
				return res;
			}
			if(version.isEmpty())
			{
				res.state="1";
				res.message="�汾�Ų���Ϊ��";
				return res;
			}
			if(deviceType.isEmpty())
			{
				res.state="1";
				res.message="�豸���Ͳ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_check_update(?,?,?,?)}");
			cs.setString(1,platformName);
			cs.setString(2,version);
			cs.setString(3, setupName);
			cs.setInt(4, Integer.parseInt(deviceType));
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			while(rs.next())
			{
				for(int i=1;i<=rsmd.getColumnCount();i++)
				{
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}	
	// ��ȡ�Ƿ������ʾ��Ϣ
	public ResultInfo getDisplay(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String platformName=json.getString("platformName");
			String setupName=json.getString("setupName");
			String deviceType=json.getString("deviceType");
			String version=json.getString("version");
			if(platformName.isEmpty())
			{
				res.state="1";
				res.message="�������Ʋ���Ϊ��";
				return res;
			}
			if(setupName.isEmpty())
			{
				res.state="1";
				res.message="��װ��������Ϊ��";
				return res;
			}
			if(deviceType.isEmpty())
			{
				res.state="1";
				res.message="�豸���Ͳ���Ϊ��";
				return res;
			}
			if(version.isEmpty())
			{
				res.state="1";
				res.message="�汾����Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_is_display(?,?,?,?)}");
			cs.setString(1,platformName);
			cs.setString(2, setupName);
			cs.setInt(3, Integer.parseInt(deviceType));
			cs.setInt(4, Integer.parseInt(version));
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			while(rs.next())
			{
				for(int i=1;i<=rsmd.getColumnCount();i++)
				{
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}	
		// ��ȡ����·��
	public ResultInfo downloadApp(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
//			String platformName=json.getString("platformName");
			String deviceType=json.getString("deviceType");
			String setupName=json.getString("setupName");
			String agentName=json.getString("agentName");
//			String version=json.getString("version");
//			String sessionKey=json.getString("cnckey");
//			String sessionValue=jedis.get(sessionKey);
//			if(sessionValue==null)
//			{
//				res.state="5";
//				res.message="�˻�δ��¼";
//				return res;
//			}
//			if(platformName.isEmpty())
//			{
//				res.state="1";
//				res.message="�������Ʋ���Ϊ��";
//				return res;
//			}
			if(deviceType.isEmpty())
			{
				res.state="1";
				res.message="�豸���Ͳ���Ϊ��";
				return res;
			}
			if(setupName.isEmpty())
			{
				res.state="1";
				res.message="��װ������Ϊ��";
				return res;
			}
			if(agentName.isEmpty())
			{
				res.state="1";
				res.message="�����̲���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_app_download(?,?,?)}");
			cs.setInt(1,Integer.parseInt(deviceType));
			cs.setString(2,setupName);
			cs.setString(3,agentName);
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			while(rs.next())
			{
				for(int i=1;i<=rsmd.getColumnCount();i++)
				{
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ���ӹ�����Ϣ
	public ResultInfo addNoticeInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String noticeTitle=json.getString("noticeTitle");
			String noticeContent=json.getString("noticeContent");
			String noticeType=json.getString("noticeType");
			String noticeSort=json.getString("noticeSort");
			String beginTime=json.getString("beginTime");
			String endTime=json.getString("endTime");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(noticeTitle.isEmpty())
			{
				res.state="1";
				res.message="���ⲻ��Ϊ��";
				return res;
			}
			if(noticeContent.isEmpty())
			{
				res.state="1";
				res.message="���ݲ���Ϊ��";
				return res;
			}
			if(noticeType.isEmpty())
			{
				res.state="1";
				res.message="���Ͳ���Ϊ��";
				return res;
			}
			if(noticeSort.isEmpty())
			{
				res.state="1";
				res.message="���в���Ϊ��";
				return res;
			}
			if(beginTime.isEmpty())
			{
				res.state="1";
				res.message="���ⲻ��Ϊ��";
				return res;
			}
			if(endTime.isEmpty())
			{
				res.state="1";
				res.message="���ݲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			int sqlResult=-1;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_add(?,?,?,?,?,?)}");
			cs.setString(1,noticeTitle);
			cs.setString(2, noticeContent);
			cs.setString(3,noticeType);
			cs.setString(4,noticeSort);
			cs.setString(5,beginTime);
			cs.setString(6,endTime);
			ResultSet rs=cs.executeQuery();
			while(rs.next())
			{
				sqlResult=rs.getInt("add_result");
			}
			if(sqlResult==1)
			{
				res.state="1";
				res.message="��������";
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	// ɾ��������Ϣ
	public ResultInfo removeNotice(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try {
			String NoticeID=json.getString("noticeID");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(NoticeID.isEmpty())
			{
				res.state="1";
				res.message="����ID����Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_remove(?)}");
			cs.setInt(1, Integer.parseInt(NoticeID));
			int row=cs.executeUpdate();
			if(row<=0)
			{
				res.state="1";
				res.message="ɾ��ʧ��";
			}
			cs.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// �޸Ĺ���
	public ResultInfo modifyNotice(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String noticeID=json.getString("noticeID");
			String noticeTitle=json.getString("noticeTitle");
			String noticeContent=json.getString("noticeContent");
			String noticeType=json.getString("noticeType");
			String noticeSort=json.getString("noticeSort");
			String beginTime=json.getString("beginTime");
			String endTime=json.getString("endTime");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(noticeID.isEmpty())
			{
				res.state="5";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			int sqlResult=0;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_modify(?,?,?,?,?,?,?)}");
			cs.setInt(1, Integer.parseInt(noticeID));
			cs.setString(2, noticeTitle);
			cs.setString(3, noticeContent);
			cs.setString(4, noticeType);
			cs.setString(5, noticeSort);
			cs.setString(6, beginTime);
			cs.setString(7, endTime);
			ResultSet rs=cs.executeQuery();
			while(rs.next())
			{
				sqlResult=rs.getInt("modify_result");
			}
			if(sqlResult==0)
			{
				CallableStatement cs1=conn.prepareCall("{call chain_futures_app.sp_notice_info(?)}");
				cs1.setInt(1, Integer.parseInt(noticeID));
				ResultSet rs1=cs1.executeQuery();
				ResultSetMetaData rsmd=rs1.getMetaData();
				while(rs1.next())
				{
					for(int i=1;i<=rsmd.getColumnCount();i++)
					{
						columnLabel=rsmd.getColumnLabel(i);
						columnValue=rs1.getString(columnLabel);
						js.put(columnLabel, columnValue);
					}
				}
				rs1.close();
				cs1.close();
			}
			else
			{
				res.state="1";
				res.message="����ʧ��";
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ��ȡ������Ϣ�б�
	public ResultInfo getNoticeList(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			int pageNum=json.getInt("pageNum");
			int pageSize=json.getInt("pageSize");
			String noticeTitle=json.getString("noticeTitle");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_list(?,?,?)}");
			cs.setInt(1, (pageNum-1)*pageSize);
			cs.setInt(2, pageSize);
			cs.setString(3, noticeTitle);
			cs.execute();
			ArrayList<JSONObject> list=new ArrayList<JSONObject>();
			int nResult=0;
			do
			{
				ResultSet rs=cs.getResultSet();
				ResultSetMetaData rsmd=rs.getMetaData();
				while(rs.next())
				{
					if(nResult==0)
					{
						for(int i=1;i<=rsmd.getColumnCount();i++)
						{
							columnLabel=rsmd.getColumnLabel(i);
							columnValue=rs.getString(columnLabel);
							js.put(columnLabel, columnValue);
						}
					}
					if(nResult==1)
					{
						JSONObject orderInfo=new JSONObject();
						for(int i=1;i<=rsmd.getColumnCount();i++)
						{
							columnLabel=rsmd.getColumnLabel(i);
							columnValue=rs.getString(columnLabel);
							orderInfo.put(columnLabel, columnValue);
						}
						list.add(orderInfo);
					}
				}
				nResult++;
				rs.close();
			}while(cs.getMoreResults());
			js.put("pageNum",pageNum);
			js.put("pageSize", pageSize);
			js.put("list", list);
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	//��ȡ������Ϣ
	public ResultInfo getNoticeInfo(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		res.data=js;
		Connection conn=null;
		try
		{
			String NoticeID=json.getString("noticeID");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(NoticeID.isEmpty())
			{
				res.state="1";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			String columnLabel=null;
			String columnValue=null;
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_info(?)}");
			cs.setInt(1, Integer.parseInt(NoticeID));
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			while(rs.next())
			{
				for(int i=1;i<=rsmd.getColumnCount();i++)
				{
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ��������
	public ResultInfo publishNotice(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			String noticeID=json.getString("noticeID");
			String noticeStatus=json.getString("noticeStatus");
			String sessionKey=json.getString("cnckey");
			String sessionValue=jedis.get(sessionKey);
			if(sessionValue==null)
			{
				res.state="5";
				res.message="�˻�δ��¼";
				return res;
			}
			if(noticeID.isEmpty())
			{
				res.state="5";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			if(noticeStatus.isEmpty())
			{
				res.state="5";
				res.message="���кŲ���Ϊ��";
				return res;
			}
			conn=cpds.getConnection();
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_publish(?,?)}");
			cs.setInt(1, Integer.parseInt(noticeID));
			cs.setInt(2, Integer.parseInt(noticeStatus));
			int row=cs.executeUpdate();
			if(row<=0)
			{
				res.state="1";
				res.message="���ķ���״̬ʧ��";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ��ȡ���¹���
	public ResultInfo getLatestNotice(JSONObject json)
	{
		ResultInfo res=new ResultInfo();
		JSONObject js=new JSONObject();
		Connection conn=null;
		res.data=js;
		try
		{
			conn=cpds.getConnection();
			CallableStatement cs=conn.prepareCall("{call chain_futures_app.sp_notice_latest()}");
			ResultSet rs=cs.executeQuery();
			ResultSetMetaData rsmd=rs.getMetaData();
			String columnLabel=null;
			String columnValue=null;
			while(rs.next()){
				for(int i=1;i<=rsmd.getColumnCount();i++){
					columnLabel=rsmd.getColumnLabel(i);
					columnValue=rs.getString(columnLabel);
					js.put(columnLabel, columnValue);
				}
			}
			rs.close();
			cs.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.state="1";
			res.message="��������";
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	// ����ֻ����Ƿ�Ϸ�
	public  boolean isMobile(String telNum)
	{
		String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        Pattern p = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(telNum);
        return m.matches();
	}

}
