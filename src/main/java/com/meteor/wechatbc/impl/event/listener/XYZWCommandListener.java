package com.meteor.wechatbc.impl.event.listener;

import cn.hutool.core.util.StrUtil;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.WeChatClient;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.MessageEvent;
import com.meteor.wechatbc.impl.model.message.ImageMessage;
import com.meteor.wechatbc.util.XYZWUtil;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @className: XYZWCommandListener
 * @author: jietao.ou
 * @date: 2024/6/26 14:55
 * @version: 1.0
 */
@AllArgsConstructor
public class XYZWCommandListener implements Listener {

	private WeChatClient weChatClient;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(20);
	
	@EventHandler
	public void onReceiveMessage(MessageEvent messageEvent){
		Message message = messageEvent.getMessage();
		Future<?> future = executorService.submit(() -> {
			if (message instanceof ImageMessage) {
				ImageMessage receiveMessageEvent = (ImageMessage) messageEvent.getMessage();
				String simpleReport = XYZWUtil.praiseSimpleReport(receiveMessageEvent.convertHexToBufferedImage());
				if (StrUtil.isNotEmpty(simpleReport)) {
					Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
					Contact sender = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getSenderUserName());
					String content = String.format("@%s  \n%s", sender.getNickName(), simpleReport);
					contact.sendMessage(content);
				}
			}
			handlerCommand(messageEvent, message);
		});
		try {
			future.get();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void handlerCommand(MessageEvent messageEvent, Message message) {
		if (message.getContent().equals("金色水晶")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t1.png"));
		}
		if (message.getContent().equals("进阶图")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t2.jpg"));
		}
		if (message.getContent().equals("梦魇水晶")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t3.jpg"));
		}
		if (message.getContent().equals("玩具扳手")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t4.png"));
		}
		if (message.getContent().equals("每日咸王")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t5.png"));
		}
		if (message.getContent().equals("排位对战")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t6.png"));
		}
		if (message.getContent().equals("红色水晶")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t7.png"));
		}
		if (message.getContent().equals("属性上限")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t8.jpg"));
		}
		if (message.getContent().equals("洗练属性")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t9.png"));
		}
		if (message.getContent().equals("洗练概率")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t10.png"));
		}
		if (message.getContent().equals("俱乐部人数")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t11.jpg"));
		}
		if (message.getContent().equals("VIP图")|| message.getContent().equals("vip图")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/t12.png"));
		}
		if (message.getContent().equals("帮助")) {
			Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
			contact.sendImage(new File("./img/xyzw/help.png"));
		}
	}
}
