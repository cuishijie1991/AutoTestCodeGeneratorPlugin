package generator.template;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;
import tools.RandomValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhaohaiyang on 2017/11/3.
 */
public class JSONProtocolArrayTestGenerator implements MethodTestGenerator {
    private PsiClass clz;
    private PsiMethod method;
    private HashMap<String, PsiField> allFields = new HashMap<>();
    private HashMap<String, PsiField> listFields = new HashMap<>();
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
                if (field.getType().getCanonicalText().contains("List")) {
                    listFields.put(field.getName(), field);
                } else {
                    allFields.put(field.getName(), field);
                }
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
        sb.append(String.format("private %s %s;\n", clz.getName(), instance));
        return sb.toString();
    }

    @Override
    public String generateBeforeMethod() {
        if (method == null) {
            return "";
        }
        sb.setLength(0);
        String paramStr = "";
        if (clz.getConstructors() != null && clz.getConstructors().length > 0) {
            PsiMethod constructor = clz.getConstructors()[0];
            PsiParameterList parameterList = constructor.getParameterList();
            if (parameterList != null && parameterList.getParameters().length > 0) {
                for (PsiParameter p : parameterList.getParameters()) {
                    paramStr += RandomValue.getRandomValueByType(p.getType()) + ", ";
                }
                if (paramStr.length() > 0) {
                    paramStr = paramStr.substring(0, paramStr.length() - 2);
                }
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
        //TODO 两种方法 一种是有list的情况 一种是没有list的情况
        List<String> testMethods = new ArrayList<>();
        if (method == null) {
            return testMethods;
        }
        testMethods.add(getListTestMethod(false));
        testMethods.add(getListTestMethod(true));
        return testMethods;
    }

    /**
     * 返回有list情况下protocol的测试语句
     *
     * @return String str
     */
    private String getListTestMethod(boolean isHasList) {
        //有list的情况
        String instance = "m" + clz.getName();
        HashMap<String, String> validExpressions = new LinkedHashMap<>();
        List<String> validListExpressions = new ArrayList<>();
        sb.setLength(0);
        if (isHasList) {
            sb.append("@org.junit.Test\n")
                    .append("public void parseFromJSONObject_has_list() throws Exception {\n");
        } else {
            sb.append("@org.junit.Test\n")
                    .append("public void parseFromJSONObject_no_list() throws Exception {\n");

        }
        //TODO 解析方法中的变量和赋值
        String body = method.getBody().getText();
        String[] splits = body.split("\n");
        for (String s : splits) {
            int k = 0;
            //只获取包含 '=' 的语句
            if (TextUtils.isEmpty(s) || !s.contains("=") || !s.contains("\"")) {
                continue;
            }
            //假设一行只有一句，根据 '=' 拆分
            String[] keys = s.split("=");
            //排除 '=='或其它情况，必须为var = expression形式
            if (keys.length != 2) {
                continue;
            }
            String var = "";
            String expression = keys[1];
            if (!s.contains("optJSONArray")) {
                var = keys[0].trim();
            } else {
                if (!expression.contains(",")) {
                    k += 1;
                    expression = expression.replace(")", String.format(", %s)", "jsonArray" + (k)));
                }
                validListExpressions.add(expression);
            }
            //参数必须是一个变量，否则抛弃
            if (TextUtils.isEmpty(var) || var.contains(" ")) {
                continue;
            }
            if (allFields.containsKey(var)) {
                if (!expression.contains(",")) {
                    expression = expression.replace(")", String.format(", %s)", var));
                }
            }
            validExpressions.put(var, expression);
        }
        String jsonObjName = null;
        if (validExpressions.size() > 0) {
            String temVar = null;
            //TODO 生成测试变量
            for (String var : validExpressions.keySet()) {
                if (temVar == null) {
                    temVar = var;
                }
                PsiField field = allFields.get(var);
                if (field != null) {
                    String s = String.format("%s %s = %s;\n", field.getType().getCanonicalText(), var, RandomValue.getRandomValueByType(field.getType()));
                    sb.append(s);
                }
            }

            //TODO 生成测试赋值语句
            String firstExpression = validExpressions.get(temVar);
            int index = firstExpression.indexOf(".");
            if (index == -1) {
                return null;
            }
            jsonObjName = firstExpression.substring(0, index);
            sb.append(String.format("org.json.JSONObject %s = new org.json.JSONObject();\n", jsonObjName));
            for (String key : validExpressions.keySet()) {
                String expression = validExpressions.get(key);
                int start = expression.indexOf("opt");
                int end = expression.indexOf("(");
                if (start > -1 && end > start) {
                    String tem = expression.substring(start, end);
                    sb.append(expression.replace(tem, "put"));
                }
            }
        }
        if (isHasList) {
            for (int j = 0; j < validListExpressions.size(); j++) {
                String expression = validListExpressions.get(j);
                int start = expression.indexOf("opt");
                int end = expression.indexOf("(");
                if (start > -1 && end > start) {
                    String tem = expression.substring(start, end);
                    String s = "org.json.JSONArray " + "jsonArray" + (j + 1) + " = new org.json.JSONArray();\n" +
                            "jsonArray" + (j + 1) + ".put(new org.json.JSONObject());\n";
                    sb.append(s);
                    sb.append(expression.replace(tem, "put"));
                }
            }
        }
        if (jsonObjName == null && validListExpressions.size() > 0) {
            String firstExpression = validListExpressions.get(0);
            int index = firstExpression.indexOf(".");
            if (index == -1) {
                return null;
            }
        }
        //TODO 生成parse语句
        sb.append(instance + ".parseFromJSONObject(" + jsonObjName + ");\n");

        //TODO 生成Assert语句
        if (validExpressions.size() > 0) {
            for (String var : validExpressions.keySet()) {
                sb.append(String.format("org.junit.Assert.assertEquals(%s, %s.%s);\n", var, instance, var));
            }
        }
        if (isHasList) {
            for (String var : listFields.keySet()) {
                sb.append(String.format("org.junit.Assert.assertEquals(\"size\", 1,%s.%s.size());\n", instance, var));
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

}
