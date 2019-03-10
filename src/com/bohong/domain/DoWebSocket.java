package com.bohong.domain;

import com.bohong.mapper.BaseMapper;
import net.sf.json.JSONObject;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InterfaceAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lenovo on 2018-8-15.
 */
@ServerEndpoint("/doEvent/{user_name}")
public class DoWebSocket {
    private SqlSessionFactory sessionFactory;
    private static SqlSession session;
    String resource = "config.xml";
    InputStream is = null;
    private BaseMapper mapper;
    private static Map<String,Session> map=new HashMap<>();
    @OnOpen
    public void onOpen(@PathParam("user_name")String userName,Session session_socket){
        System.out.println(userName+"建立连接");
        map.put(userName,session_socket);
        setMapper();
        System.out.println("当前连接数："+map.size());
    }
    @OnMessage
    public void onMessage(String msg,Session session_socket,@PathParam("user_name")String user_name) throws IOException {
        Base base=new Base();
        if(session_socket.isOpen()){
            System.out.println("消息="+msg);
            if(!"undefined".equals(msg)){
                JSONObject jsonObject=JSONObject.fromObject(msg);
                String toSendUser;
                String message=jsonObject.get("message").toString();
                 toSendUser=jsonObject.get("receiver").toString();
                 String[] toSend=toSendUser.split(",");

                 for(int i=0;i<toSend.length;i++){
                     base.setSender(user_name);
                     base.setReceivedBy(toSend[i]);
                     addMessage(base,user_name,message);
                 }

            }
        }
    }
    @OnClose
    public void onClose(@PathParam("user_name")String user_name){
        System.out.println(user_name+"断开连接");
        map.remove(user_name);
        System.out.println("当前连接数："+map.size());
    }
    @OnError
    public void onError(Throwable throwable,@PathParam("user_name")String user_name){
        if(throwable.getLocalizedMessage().contains("The client aborted the connection")){
            System.out.println(user_name+"客户端关闭连接");
        }else{
            System.out.println("-------------------------------webSocket问题----------------------------");
            System.out.println(user_name+"websocket报错");
            throwable.printStackTrace();
            System.out.println("-------------------------------完毕---------------------------------");
        }

    }
    private void setMapper(){
        try {
            is = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sessionFactory = new SqlSessionFactoryBuilder().build(is);
        session = sessionFactory.openSession();
        mapper = session.getMapper(BaseMapper.class);
    }
    private void add(Base base){
        mapper.addMessage(base);
        session.commit();
        session.close();
        System.out.println("操作完毕");
    }
    private void addMessage(Base workMessage, String user_name,String message) throws IOException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String t=sdf.format(new Date());
        workMessage.setMessage_time(t);

        workMessage.setMessage(user_name+" 给您发送一条消息:\n"+message);

        if(map.get(workMessage.getReceivedBy())!=null){//在线
            workMessage.setOnLine(true);
            add(workMessage);
            map.get(workMessage.getReceivedBy()).getBasicRemote().sendText(workMessage.getMessage());
        }else{//离线
            workMessage.setOnLine(false);
            add(workMessage);
        }
    }
}
