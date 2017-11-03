package generator.template;

import com.intellij.psi.PsiClass;

import java.util.List;

/**
 * Created by shijiecui on 2017/11/3.
 */
public class JSONProtocolTestGenerator implements MethodTestGenerator {
    public final static String Identifier = "BaseProtocol";

    //TODO
    @Override
    public MethodTestGenerator setClass(PsiClass clz) {
        return this;
    }

    @Override
    public String generateFields() {
        return null;
    }

    @Override
    public String generateBeforeMethod() {
        return null;
    }

    @Override
    public List<String> generateTestMethod() {
        return null;
    }
}
