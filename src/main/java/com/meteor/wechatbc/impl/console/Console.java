package com.meteor.wechatbc.impl.console;

import com.alibaba.fastjson2.JSONObject;
import com.meteor.wechatbc.command.WeChatCommand;
import com.meteor.wechatbc.command.sender.ConsoleSender;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.entitiy.contact.GetBatchContact;
import com.meteor.wechatbc.entitiy.message.SentMessage;
import com.meteor.wechatbc.impl.WeChatClient;
import com.meteor.wechatbc.impl.command.CommandManager;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import java.util.Optional;

/**
 * 控制台
 */
public class Console extends SimpleTerminalConsole {

    private final WeChatClient weChatClient;

    public Console(WeChatClient weChatClient){
        this.weChatClient = weChatClient;
        this.consoleSender = new ConsoleSender(weChatClient);
    }

    private final ConsoleSender consoleSender; // 控制台指令执行者

    @Override
    protected boolean isRunning() {
        return true;
    }

    @Override
    protected void runCommand(String command) {
        CommandManager commandManager = weChatClient.getCommandManager();
        CommandManager.ExecutorCommand executorCommand = commandManager.getExecutorCommand(command);
        Optional.ofNullable(commandManager.getWeChatCommandMap().get(executorCommand.getMainCommand())).ifPresent(weChatCommand -> {
            weChatCommand.getCommandExecutor().onCommand(consoleSender, executorCommand.formatArgs());
        });
    }

    @Override
    protected void shutdown() {
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return builder.history(new DefaultHistory()).build();
    }
}
