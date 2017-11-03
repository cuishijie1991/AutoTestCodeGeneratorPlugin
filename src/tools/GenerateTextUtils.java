package tools;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;


/**
 * Created by shijiecui on 2017/11/3.
 */
public final class GenerateTextUtils {
    static StringBuilder sb = new StringBuilder();

    public static String getBeforeMethod(String methodText) {
        if (methodText == null) {
            methodText = "";
        }
        sb.setLength(0);
        sb.append("@org.junit.Before\n")
                .append("public void setUp(){\n")
                .append(methodText)
                .append("}\n");
        return sb.toString();
    }

    public static String getTestMethod(String methodName, String methodText) {
        if (methodText == null) {
            methodText = "";
        }
        if (TextUtils.isEmpty(methodName)) {
            methodName = "method";
        }
        sb.setLength(0);
        sb.append("@org.junit.Test\n")
                .append("public void ").append(methodName).append("() throws Exception {\n")
                .append(methodText)
                .append("}\n");
        return sb.toString();
    }

    public static String getInstanceDeclarationText(PsiClass clz, String instance) {
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
            return String.format("%s = new %s(%s);", instance, clz.getName(), paramStr);
        }
        return "";
    }

    public static String getFieldDeclarationText(PsiField field, String var, boolean isGlobal) {
        sb.setLength(0);
        if (field != null) {
            if (isGlobal) {
                sb.append(field.getType().getCanonicalText()).append(" ");
            }
            sb.append(var)
                    .append(" = ")
                    .append(RandomValue.getRandomValueByType(field.getType()))
                    .append(";\n");
        }
        return sb.toString();
    }

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
