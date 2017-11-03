package generator;

import com.intellij.psi.*;
import generator.template.MethodTestGeneratorFactory;
import generator.template.MethodTestGenerator;
import tools.CodeInsert;

import java.util.List;

/**
 * Created by shijiecui on 2017/11/1.
 */
public class CodeWriterImpl implements CodeWriter {
    private PsiClass clz;
    private PsiFile target;
    private CodeInsert insert;
    private MethodTestGenerator testGenerator;


    @Override
    public CodeWriter setClass(PsiClass clz, PsiFile target) {
        this.clz = clz;
        this.target = target;
        return this;
    }

    @Override
    public void generateTestCode() {
        insert = CodeInsert.getInstance(target);
        testGenerator = MethodTestGeneratorFactory.getMethodGenerator(clz);
        _createModelTest(testGenerator, insert);
    }

    private void _createModelTest(MethodTestGenerator testGenerator, CodeInsert insert) {
        insert.insertFiled(testGenerator.generateFields());
        insert.insertMethod(testGenerator.generateBeforeMethod());
        List<String> testMethods = testGenerator.generateTestMethod();
        for (String method : testMethods) {
            insert.insertMethod(method);
        }
        insert.checkSetClassModifier(PsiModifier.PUBLIC);
    }

}
