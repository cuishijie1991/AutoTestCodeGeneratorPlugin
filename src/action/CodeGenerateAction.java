package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import generator.CodeGenerator;
import generator.CodeGeneratorImpl;
import tools.FileCreator;
import org.jetbrains.annotations.NotNull;

/**
 * Created by shijiecui on 2017/11/1.
 */
public class CodeGenerateAction extends AnAction {

    Editor editor;
    Project project;
    PsiFile psiFile;
    PsiClass clz;
    CodeGenerator codeGenerator;

    @Override
    public void actionPerformed(AnActionEvent e) {
        editor = e.getData(PlatformDataKeys.EDITOR);
        project = e.getData(PlatformDataKeys.PROJECT);
        psiFile = e.getData(PlatformDataKeys.PSI_FILE);

        for (PsiElement element : psiFile.getChildren()) {
            if (element instanceof PsiClass) {
                this.clz = (PsiClass) element;
                break;
            }
        }
        codeGenerator = new CodeGeneratorImpl();
        PsiFile targetFile = FileCreator.checkAndCreateTestFile(psiFile.getContainingDirectory(), clz);
        if (targetFile == null) {
            System.out.print("failed to create test file !");
            return;
        }

        new WriteCommandAction.Simple(project, targetFile) {

            @Override
            protected void run() throws Throwable {
                codeGenerator.setClass(clz, targetFile).generateCode();
                FileCreator.openFileInIde(targetFile);
                //执行完任务设置action失效，以免触发update(action)
                e.getPresentation().setEnabled(false);
            }
        }.execute();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        try {
            editor = e.getData(PlatformDataKeys.EDITOR);
            project = e.getData(PlatformDataKeys.PROJECT);
            psiFile = e.getData(PlatformDataKeys.PSI_FILE);
            if (!psiFile.getName().endsWith(".java")) {
                e.getPresentation().setEnabled(false);
            } else {
                e.getPresentation().setEnabled(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
