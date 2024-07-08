package com.meteor.wechatbc.xyzw;

import lombok.Getter;

/**
 * @className: CommandEnum
 * @author: jietao.ou
 * @date: 2024/6/28 16:57
 * @version: 1.0
 */
public enum CommandEnum {
	t0("帮助",1,"./img/xyzw/help.png"),
	t1("金色水晶",1,"./img/xyzw/t1.png"),
	t2("进阶图",1,"./img/xyzw/t2.jpg"),
	t3("梦魇水晶",1,"./img/xyzw/t3.png"),
	t4("玩具扳手",1,"./img/xyzw/t4.png"),
	t5("每日咸王",1,"./img/xyzw/t5.png"),
	t6("排位对战",1,"./img/xyzw/t6.png"),
	t7("红色水晶",1,"./img/xyzw/t7.png"),
	t8("属性上限",1,"./img/xyzw/t8.jpg"),
	t9("洗练属性",1,"./img/xyzw/t9.png"),
	t10("洗练概率",1,"./img/xyzw/t10.png"),
	t11("俱乐部人数",1,"./img/xyzw/t11.jpg"),
	t12("VIP图",1,"./img/xyzw/t12.png"),
	t12_1("vip图",1,"./img/xyzw/t12.png"),
	t13("金币图",1,"./img/xyzw/t13.jpg"),
	t14("礼包码",2,null,"taptap666、VIP666、vip666、XY888、QQXY888、happy666、HAPPY666、xyzwgame666、douyin666、douyin777、douyin888、huhushengwei888、APP666、app666"),
	
	;
	@Getter
	private String name;
	/**
	 * 指令类型：1 返回预设图片  2 返回预设文字
	 */
	@Getter
	private Integer type;
	/**
	 * 预设图片地址
	 * @see img/xyzw
	 */
	@Getter
	private String path;
	
	@Getter
	private String text;

	CommandEnum(String name, Integer type, String path, String text) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.text = text;
	}

	CommandEnum(String name, Integer type, String path) {
		this.name = name;
		this.type = type;
		this.path = path;
	}
	
	public static CommandEnum findByName(String name){
		for (CommandEnum commandEnum : CommandEnum.values()) {
			if (commandEnum.getName().equals(name)){
				return commandEnum;
			}
		}
		return null;
	}
}
