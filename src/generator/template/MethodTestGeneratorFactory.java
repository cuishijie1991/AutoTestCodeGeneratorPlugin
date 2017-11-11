package generator.template;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import tools.RandomValue;

/**
 * Created by shijiecui on 2017/11/3.
 */
public class MethodTestGeneratorFactory {
    public static MethodTestGenerator getMethodGenerator(PsiClass clz) {
        MethodTestGenerator generator = null;
        boolean hasArray = false;
        boolean hasSubModel = false;
        for (PsiField field : clz.getAllFields()) {
            if ("null".equals(RandomValue.getRandomValueByType(field.getType()))) {
                hasSubModel = true;
                break;
            }
            if (field.getType().getCanonicalText().contains("List")) {
                hasArray = true;
                break;
            }
        }
        if (clz.getSuperClass().getName().equals("BaseProtocol")) {
            generator = hasArray ? new JSONProtocolArrayTestGenerator() : new JSONProtocolModelTestGenerator();
        } else {
            for (PsiClass interfaces : clz.getInterfaces()) {
                if (interfaces.getName().equals("IParseFromJSONObject")) {
                    if (hasArray) {
                        generator = new JSONModelArrayTestGenerator();
                    } else if (hasSubModel) {
                        generator = new JSONModelModelTestGenerator();
                    } else {
                        generator = new JSONModelTestGenerator();
                    }
                    break;
                }
            }
        }
        if (generator == null) {
            generator = new SimpleTemplateTestGenerator();
        }
        return generator.setClass(clz);
    }
}
