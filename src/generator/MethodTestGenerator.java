package generator;

import com.intellij.psi.PsiClass;

import java.util.List;

/**
 * Created by shijiecui on 2017/11/2.
 */
public interface MethodTestGenerator {

    MethodTestGenerator setClass(PsiClass clz);

    String generateFields();

    String generateBeforeMethod();

    List<String> generateTestMethod();
}
