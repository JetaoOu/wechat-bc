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
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private final Logger logger = LogManager.getLogger("XYZWCommandListener");
	private WeChatClient weChatClient;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(20);
	
	@EventHandler
	public void onReceiveMessage(MessageEvent messageEvent){
		Message message = messageEvent.getMessage();
		executorService.submit(() -> {
			try {
				if (message instanceof ImageMessage) {
					ImageMessage receiveMessageEvent = (ImageMessage) messageEvent.getMessage();
					String simpleReport = XYZWUtil.praiseSimpleReport(receiveMessageEvent.convertHexToBufferedImage());
					if (StrUtil.isNotEmpty(simpleReport)) {
						Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
						Contact sender = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getSenderUserName());
						final Contact groupContact = messageEvent.getEventManager().getWeChatClient().getContactManager().getGroupContact(message.getFromUserName());
						final Contact.ContactMember groupMemberUser = groupContact.findGroupMemberUser(message.getSenderUserName());
						String content = String.format("@%s  \n%s", groupMemberUser.getDisplayName(), simpleReport);
						contact.sendMessage(content);
					}
				}else {
					handlerCommand(messageEvent, message);
				}
			} catch (Exception e) {
				logger.warn("处理消息事件时发生异常", e);
			}
		});
	}

	/**
	 * 处理消息事件中的命令。
	 * 根据消息内容中的命令类型，执行相应的操作，如发送图片或文本消息。
	 * 
	 * @param messageEvent 消息事件对象，包含事件相关的信息。
	 * @param message 消息对象，包含消息的具体内容。
	 */
	private void handlerCommand(MessageEvent messageEvent, Message message) {
	    // 根据消息内容查找对应的命令枚举
	    CommandEnum commandEnum = CommandEnum.findByName(message.getContent());
	    // 如果找到了有效的命令枚举
	    if (ObjUtil.isNotEmpty(commandEnum)){
	        // 根据命令类型执行不同的操作
	        switch (commandEnum.getType()){
	            case 1:
	                // 发送图片命令
	                {
	                    // 获取发送方的联系人对象
	                    Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
	                    // 根据命令枚举中的路径发送图片
	                    contact.sendImage(new File(commandEnum.getPath()));
	                    break;
	                }
	            case 2:
	                // 发送文本消息命令
	                {
	                    // 获取发送方的联系人对象
	                    Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
	                    // 根据命令枚举中的文本内容发送消息
	                    contact.sendMessage(commandEnum.getText());
	                    break;
	                }
	            default:
	                // 对于未知的命令类型，不做任何处理
	                break;
	        }
	    }
	}
}
