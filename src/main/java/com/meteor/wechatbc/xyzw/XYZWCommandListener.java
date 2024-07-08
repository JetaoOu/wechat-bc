package com.meteor.wechatbc.xyzw;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.WeChatClient;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.MessageEvent;
import com.meteor.wechatbc.impl.model.message.ImageMessage;
import com.meteor.wechatbc.xyzw.XYZWUtil;
import lombok.AllArgsConstructor;

import java.io.File;
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
			}else {
				handlerCommand(messageEvent, message);
			}
		});
		try {
			future.get();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void handlerCommand(MessageEvent messageEvent, Message message) {
		CommandEnum commandEnum = CommandEnum.findByName(message.getContent());
		if (ObjUtil.isNotEmpty(commandEnum)){
			switch (commandEnum.getType()){
				case 1:{
					Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
					contact.sendImage(new File(commandEnum.getPath()));
					break;
				}
				case 2:{
					Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
					contact.sendMessage(commandEnum.getText());
					break;
				}
				default:
					break;
			}
		}		
	}
}
