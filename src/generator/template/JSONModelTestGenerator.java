package generator.template;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;
import tools.GenerateTextUtils;
import tools.RandomValue;

import java.util.*;

/**
 * Created by shijiecui on 2017/11/2.
 */
public class JSONModelTestGenerator implements MethodTestGenerator {
    protected PsiClass clz;
    protected PsiMethod method;
    protected HashMap<String, PsiField> allFields = new HashMap<>();
    protected StringBuilder sb = new StringBuilder();
    protected String instance;

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
        return GenerateTextUtils.getFieldDeclarationText(clz.getName(), instance);
    }

    @Override
    public String generateBeforeMethod() {
        if (method == null) {
            return "";
        }
        return GenerateTextUtils.getBeforeMethod(GenerateTextUtils.getInstanceDeclarationText(clz, instance));
    }

    @Override
    public List<String> generateTestMethod() {
        List<String> testMethods = new ArrayList<>();
        if (method == null) {
            return testMethods;
        }
        HashMap<String, String> validExpressions = new LinkedHashMap<>();
        sb.setLength(0);
        //TODO 解析方法中的变量和赋值
        String body = method.getBody().getText();
        String[] splits = body.split("\n");
        //筛选语句类型 evaluator_id = result.optInt("evaluator_id", evaluator_id);
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
            //'='右边表达式必须含有'.'否则可能不是result.optInt("evaluator_id", evaluator_id)型赋值语句，需要抛弃
            if (TextUtils.isEmpty(expression) || !expression.contains(".")) {
                continue;
            }
            //先查找JsonArray
            if (var.contains("JSONArray")) {
                //TODO
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
        //TODO 生成测试变量
        for (String var : validExpressions.keySet()) {
            PsiField field = allFields.get(var);
            String s = String.format("%s %s = %s;\n", field.getType().getCanonicalText(), var, RandomValue.getRandomValueByType(field.getType()));
            sb.append(s);
        }

        //TODO 生成测试赋值语句
        sb.append("org.json.JSONObject jo = new org.json.JSONObject();\n");
        for (String expression : validExpressions.values()) {
            int start = 0;
            int end = expression.indexOf("(");
            if (end > start) {
                String tem = expression.substring(start, end);
                sb.append(expression.replace(tem, " jo.put"));
            }
        }

        //TODO 生成parse语句
        sb.append(instance + ".parseFromJSONObject(jo);\n");

        //TODO 生成Assert语句
        for (String var : validExpressions.keySet()) {
            sb.append(GenerateTextUtils.getAssertExpression(var, instance + "." + var));
        }
        testMethods.add(GenerateTextUtils.getTestMethod("parseFromJSONObject", sb.toString()));
        return testMethods;
    }


}
