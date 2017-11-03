package generator;

import com.intellij.psi.*;

/**
 * Created by shijiecui on 2017/11/1.
 */
public class CodeGeneratorImpl implements CodeGenerator {
    private PsiClass clz;
    private PsiFile target;
    private CodeWriter writer;

    public CodeGeneratorImpl() {
        writer = new JunitCodeWriterImpl();
    }

    @Override
    public CodeGenerator setClass(PsiClass clz, PsiFile target) {
        this.clz = clz;
        this.target = target;
        writer.setClass(clz, target);
        return this;
    }

    @Override
    public void generateCode() {
        writer.generateTestCode();
    }


}
