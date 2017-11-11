package generator.template;

import com.intellij.psi.*;
import tools.GenerateTextUtils;
import tools.RandomValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shijiecui on 2017/11/11.
 */
public class SimpleTemplateTestGenerator implements MethodTestGenerator {
    protected PsiClass clz;
    protected List<PsiMethod> methods = new ArrayList<>();
    protected StringBuilder sb = new StringBuilder();
    protected String instance;

    @Override
    public MethodTestGenerator setClass(PsiClass clz) {
        this.clz = clz;
        instance = "m" + clz.getName();
        for (PsiMethod method : clz.getMethods()) {
            if (!method.isConstructor() && !method.hasModifierProperty(PsiModifier.PRIVATE)) {
                methods.add(method);
            }
        }
        return this;
    }

    @Override
    public String generateFields() {
        return GenerateTextUtils.getFieldDeclarationText(clz.getName(), instance);
    }

    @Override
    public String generateBeforeMethod() {
        return GenerateTextUtils.getBeforeMethod(GenerateTextUtils.getInstanceDeclarationText(clz, instance));
    }


    @Override
    public List<String> generateTestMethod() {
        List<String> methodList = new ArrayList<>();
        for (PsiMethod method : methods) {
            sb.setLength(0);
            sb.append(instance).append(".").append(method.getName()).append("(");
            PsiParameterList parameterList = method.getParameterList();
            String paramStr = "";
            for (PsiParameter p : parameterList.getParameters()) {
                paramStr += RandomValue.getRandomValueByType(p.getType()) + ", ";
            }
            if (paramStr.length() > 0) {
                paramStr = paramStr.substring(0, paramStr.length() - 2);
            }
            sb.append(paramStr).append(");\n");
            methodList.add(GenerateTextUtils.getTestMethod(method.getName(), sb.toString()));
        }
        return methodList;
    }
}
