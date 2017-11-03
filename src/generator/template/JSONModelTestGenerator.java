package generator.template;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;
import tools.RandomValue;

import java.util.*;

/**
 * Created by shijiecui on 2017/11/2.
 */
public class JSONModelTestGenerator implements MethodTestGenerator {
    private PsiClass clz;
    private PsiMethod method;
    private HashMap<String, PsiField> allFields = new HashMap<>();
    private StringBuilder sb = new StringBuilder();
    private String instance;

    @Override
    public MethodTestGenerator setClass(PsiClass clz) {
        this.clz = clz;
        instance = "m" + clz.getName();
        for (PsiMethod method : clz.getMethods()) {
            if (method.getName().equals("parseFromJSONObject")) {
                this.method = method;
                break;
            }
        }
        if (method != null) {
            for (PsiField field : clz.getFields()) {
                if (field.hasModifierProperty("private")
                        || field.hasModifierProperty("final")
                        || field.hasModifierProperty("static")) {
                    continue;
                }
                allFields.put(field.getName(), field);
            }
        }
        return this;
    }

    @Override
    public String generateFields() {
        if (method == null) {
            return "";
        }
        sb.setLength(0);
        sb.append(String.format("%s %s;\n", clz.getName(), instance));
        return sb.toString();
    }

    @Override
    public String generateBeforeMethod() {
        if (method == null) {
            return "";
        }
        sb.setLength(0);
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


        sb.append("@org.junit.Before\n")
                .append("public void setUp(){\n")
                .append(String.format("%s = new %s(%s);\n", instance, clz.getName(), paramStr))
                .append("}\n");
        return sb.toString();
    }

    @Override
    public List<String> generateTestMethod() {
        List<String> testMethods = new ArrayList<>();
        if (method == null) {
            return testMethods;
        }
        String instance = "m" + clz.getName();
        HashMap<String, String> validExpressions = new LinkedHashMap<>();
        sb.setLength(0);
        sb.append("@org.junit.Test\n")
                .append("public void parseFromJSONObject() throws Exception {\n");
        //TODO 解析方法中的变量和赋值
        String body = method.getBody().getText();
        String[] splits = body.split("\n");
        for (String s : splits) {
            //只获取包含 '=' 的语句
            if (TextUtils.isEmpty(s) || !s.contains("=")) {
                continue;
            }
            //假设一行只有一句，根据 '=' 拆分
            String[] keys = s.split("=");
            //排除 '=='或其它情况，必须为var = expression形式
            if (keys.length != 2) {
                continue;
            }
            String var = keys[0].trim();
            String expression = keys[1];
            //参数必须是一个变量，否则抛弃
            if (TextUtils.isEmpty(var) || var.contains(" ")) {
                continue;
            }
            if (allFields.containsKey(var)) {
                if (!expression.contains(",")) {
                    expression = expression.replace(")", String.format(", %s)", var));
                }
                validExpressions.put(var, expression);
            }
        }
        if (validExpressions.size() == 0) {
            return testMethods;
        }
        String temVar = null;
        String jsonObjName = null;

        //TODO 生成测试变量
        for (String var : validExpressions.keySet()) {
            if (temVar == null) {
                temVar = var;
            }
            PsiField field = allFields.get(var);
            String s = String.format("%s %s = %s;\n", field.getType().getCanonicalText(), var, RandomValue.getRandomValueByType(field.getType()));
            sb.append(s);
        }

        //TODO 生成测试赋值语句
        String firstExpression = validExpressions.get(temVar);
        int index = firstExpression.indexOf(".");
        if (index == -1) {
            return testMethods;
        }
        jsonObjName = firstExpression.substring(0, index);
        sb.append(String.format("org.json.JSONObject %s = new org.json.JSONObject();\n", jsonObjName));
        for (String expression : validExpressions.values()) {
            int start = expression.indexOf("opt");
            int end = expression.indexOf("(");
            if (start > -1 && end > start) {
                String tem = expression.substring(start, end);
                sb.append(expression.replace(tem, "put"));
            }
        }

        //TODO 生成parse语句
        sb.append(instance + ".parseFromJSONObject(" + jsonObjName + ");\n");

        //TODO 生成Assert语句
        for (String var : validExpressions.keySet()) {
            sb.append(String.format("org.junit.Assert.assertEquals(%s, %s.%s);\n", var, instance, var));
        }
        sb.append("}\n");
        testMethods.add(sb.toString());
        return testMethods;
    }


}
