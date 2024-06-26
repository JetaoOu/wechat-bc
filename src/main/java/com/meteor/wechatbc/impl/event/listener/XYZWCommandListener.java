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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		executorService.submit(()->{
			if (message instanceof ImageMessage){
				ImageMessage receiveMessageEvent = (ImageMessage) messageEvent.getMessage() ;
				String simpleReport = XYZWUtil.praiseSimpleReport(receiveMessageEvent.convertHexToBufferedImage());
				if (StrUtil.isNotEmpty(simpleReport)) {
					Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
					Contact sender = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getSenderUserName());
					String content = String.format("@%s  \n%s",sender.getNickName(),simpleReport);
					contact.sendMessage(content);
				}
			}
			if (message.getContent().equals("帮助")) {
				Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
				contact.sendMessage("只算金鱼宝箱，想加功能就打钱");
			}
		});
	}
}
