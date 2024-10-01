package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;

@Slf4j
public class CommandVc extends CommandInformation {

  private AudioManager audioManager;

  @Override
  public String getCommandName() {
    return "vc";
  }

  @Override
  protected String getCommandDescription() {
    return "Botが現在VCに入室しているかどうか自動で判断し、入っていない場合は入室して読み上げを開始、入っている場合は退出して読み上げの終了処理を行います。";
  }

  @Override
  public void setCommandData() {
    this.commandData = Commands.slash(
        this.getCommandName(),
        this.getCommandDescription()
    );
  }
}
