package generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

/**
 * Created by shijiecui on 2017/11/1.
 */
public interface CodeGenerator {

    CodeGenerator setClass(PsiClass clz, PsiFile target);

    void generateCode();
}
