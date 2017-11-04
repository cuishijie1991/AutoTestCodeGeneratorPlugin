package tools;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;


/**
 * Created by shijiecui on 2017/11/3.
 */
public final class GenerateTextUtils {
    static StringBuilder sb = new StringBuilder();

    /**
     * @param methodBody
     * @return
     */
    public static String getBeforeMethod(String methodBody) {
        if (methodBody == null) {
            methodBody = "";
        }
        sb.setLength(0);
        sb.append("@org.junit.Before\n")
                .append("public void setUp(){\n")
                .append(methodBody)
                .append("}\n");
        return sb.toString();
    }

    /**
     * @param methodName
     * @param methodBody
     * @return
     */
    public static String getTestMethod(String methodName, String methodBody) {
        if (methodBody == null) {
            methodBody = "";
        }
        if (TextUtils.isEmpty(methodName)) {
            methodName = "method";
        }
        sb.setLength(0);
        sb.append("@org.junit.Test\n")
                .append("public void ").append(methodName).append("() throws Exception {\n")
                .append(methodBody)
                .append("}\n");
        return sb.toString();
    }

    /**
     * 解析构造函数进行实例化，a = new A(param...);
     *
     * @param clz
     * @param instanceName
     * @return
     */
    public static String getInstanceDeclarationText(PsiClass clz, String instanceName) {
        if (clz != null) {
            String paramStr = "";
            PsiMethod[] constructors = clz.getConstructors();
            if (constructors.length > 0) {
                PsiMethod constructor = constructors[0];
                PsiParameterList parameterList = constructor.getParameterList();

                for (PsiParameter p : parameterList.getParameters()) {
                    paramStr += RandomValue.getRandomValueByType(p.getType()) + ", ";
                }
                if (paramStr.length() > 0) {
                    paramStr = paramStr.substring(0, paramStr.length() - 2);
                }
            }
            return String.format("%s = new %s(%s);\n", instanceName, clz.getName(), paramStr);
        }
        return "";
    }

    /**
     * a = value;
     * A a = value;
     *
     * @param field
     * @param var
     * @param isGlobal 是否需要声明变量类型
     * @return
     */
    public static String getFieldDeclarationText(PsiField field, String var, boolean isGlobal) {
        sb.setLength(0);
        PsiType type = field.getType();
        String value = RandomValue.getRandomValueByType(type);
        if (field != null) {
            if (isGlobal) {
                if (value == "null") {
                    //model类型需要手动去引包
                    PsiClass fClz = JavaPsiFacade.getInstance(field.getProject()).findClass(field.getType().getInternalCanonicalText(), field.getResolveScope());
                    if (fClz != null) {
                        String packageRef = ((PsiJavaFile) fClz.getContainingFile()).getPackageName() + ".";
                        sb.append(packageRef);
                    }
                }
                sb.append(field.getType().getCanonicalText()).append(" ");
            }
            sb.append(var).append(" = ").append(value).append(";\n");
        }
        return sb.toString();
    }

    /**
     * @param type
     * @param var
     * @return
     */
    public static String getFieldDeclarationText(String type, String var) {
        return String.format("%s %s;\n", type, var);
    }

    public static String getAssertExpression(String arg1, String arg2) {
        if (TextUtils.isEmpty(arg1)) {
            arg1 = "arg1";
        }
        if (TextUtils.isEmpty(arg2)) {
            arg2 = "arg2";
        }
        return String.format("org.junit.Assert.assertEquals(%s, %s);\n", arg1, arg2);
    }
}
