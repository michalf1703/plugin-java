import com.intellij.openapi.actionSystem.AnAction;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class MyPluginAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

        Messages.showMessageDialog("Witaj z pluginu IntelliJ!", "Plugin Info", Messages.getInformationIcon());
    }
}