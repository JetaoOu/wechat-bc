package com.meteor.wechatbc.impl.plugin;

import com.meteor.wechatbc.Main;
import com.meteor.wechatbc.command.WeChatCommand;
import com.meteor.wechatbc.impl.WeChatClient;
import com.meteor.wechatbc.plugin.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 */
public class PluginManager {

    private Map<String,BasePlugin> pluginMap = new ConcurrentHashMap<>();


    private WeChatClient weChatClient;

    public PluginManager(WeChatClient weChatClient){
        this.weChatClient = weChatClient;
        // 创建plugins目录存放插件
        File pluginsFolder = new File(System.getProperty("user.dir"),"plugins");
        if(!pluginsFolder.exists()){
            pluginsFolder.mkdirs();
        }
        PluginLoader.logger.info("开始载入插件");
        for (File pluginFile : pluginsFolder.listFiles()) {
            if(pluginFile.isFile()){
                this.loadPlugin(pluginFile);
            }
        }
        PluginLoader.logger.info("载入了 {} 个插件",pluginMap.size());
    }

    /**
     * 卸载插件
     */
    public void unload(BasePlugin plugin){
        String pluginName = plugin.getPluginDescription().getName();
        weChatClient.getEventManager().unRegisterPluginListener(plugin);
        pluginMap.remove(plugin,pluginName);
        PluginLoader.logger.info("已卸载 {}",pluginName);
    }

    /**
     * 加载插件
     * @param file
     */
    public void loadPlugin(File file){
        PluginLoader pluginLoader = new PluginLoader(file);
        // 获取插件描述信息
        PluginDescription pluginDescription = pluginLoader.getPluginDescription();
        // 如果插件已加载，则终止后面的逻辑
        if(pluginMap.containsKey(pluginDescription.getName())){
            PluginLoader.logger.info("插件 [{}] 已存在，无法重新加载",pluginDescription.getName());
            return;
        }


        PluginLoader.logger.info("正在载入插件{}",pluginDescription.getName());

        URL[] urls = new URL[0];
        try {
            urls = new URL[]{ file.toURI().toURL() };
            // 为每个插件单独开一个类加载器以隔离
            PluginClassLoader pluginClassLoader = new PluginClassLoader(urls, Main.class.getClassLoader());
            // 加载插件主类
            Class<?> mainClass = Class.forName(pluginDescription.getMain(), true, pluginClassLoader);
            // 实例化插件主类
            BasePlugin plugin = (BasePlugin) mainClass.getDeclaredConstructor().newInstance();
            // 如果主类不是BasePlugin的子类
            if (!BasePlugin.class.isAssignableFrom(mainClass)) {
                throw new IllegalArgumentException("加载插件时发生了一个错误,主类必须继承自 BasePlugin " + pluginDescription.getMain());
            }

            if(pluginDescription.getCommands()!=null){
                // 解析指令并创建实例
                pluginDescription.getCommands().forEach(mainCommand->{
                    WeChatCommand weChatCommand = new WeChatCommand(mainCommand);
                    PluginLoader.logger.info(" 解析指令 {}",weChatCommand.getMainCommand());
                    weChatClient.getCommandManager().registerCommand(weChatCommand);
                });
            }
            pluginMap.put(pluginDescription.getName(),plugin);
            // 初始化插件
            PluginLoader.logger.info(" 初始化 {}",pluginDescription.getMain());
            plugin.init(pluginDescription,weChatClient);
            plugin.onEnable();
            PluginLoader.logger.info("已载入 {}",pluginDescription.getName());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
