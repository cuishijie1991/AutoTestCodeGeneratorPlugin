package generator.template;

import com.intellij.psi.PsiClass;

/**
 * Created by shijiecui on 2017/11/5.
 */
public class JSONModelModelTestGenerator extends JSONProtocolModelTestGenerator {
    @Override
    public MethodTestGenerator setClass(PsiClass clz) {
        super.setClass(clz);
        instance = "m" + clz.getName();
        return this;
    }
}
