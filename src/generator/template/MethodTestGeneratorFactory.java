package generator.template;

import com.intellij.psi.PsiClass;

/**
 * Created by shijiecui on 2017/11/3.
 */
public class MethodTestGeneratorFactory {
    public static MethodTestGenerator getMethodGenerator(PsiClass clz) {
        MethodTestGenerator generator;
        if (clz.getName().toLowerCase().contains("protocol")) {
            generator = new JSONProtocolTestGenerator();
        } else {
            generator = new JSONModelTestGenerator();
        }
        return generator.setClass(clz);
    }
}
