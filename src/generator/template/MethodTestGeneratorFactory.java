package generator.template;

import com.intellij.psi.PsiClass;

/**
 * Created by shijiecui on 2017/11/3.
 */
public class MethodTestGeneratorFactory {
    public static MethodTestGenerator getMethodGenerator(PsiClass clz) {
        MethodTestGenerator generator = null;
        if (clz.getSuperClass().getName().equals("BaseProtocol")) {
            generator = new JSONProtocolWithModelTestGenerator();
        } else {
            for (PsiClass interfaces : clz.getInterfaces()) {
                if (interfaces.getName().equals("IParseFromJSONObject")) {
                    generator = new JSONModelTestGenerator();
                    break;
                }
            }
        }
        return generator == null ? null : generator.setClass(clz);
    }
}
