package generator.template;

import com.intellij.psi.*;
import org.apache.http.util.TextUtils;
import tools.GenerateTextUtils;
import tools.RandomValue;

import java.util.*;

/**
 * Created by shijiecui on 2017/11/4.
 */
public class JSONProtocolModelTestGenerator extends JSONModelTestGenerator {
    private HashMap<String, PsiClass> modelFields = new LinkedHashMap<>();

    @Override
    public MethodTestGenerator setClass(PsiClass clz) {
        super.setClass(clz);
        instance = "protocol";
        //解析出model类型的成员变量，保存起来
        for (PsiField field : clz.getFields()) {
            if (RandomValue.getRandomValueByType(field.getType()).equals("null")) {
                PsiClass fClz = JavaPsiFacade.getInstance(field.getProject()).findClass(field.getType().getInternalCanonicalText(), field.getResolveScope());
                if (fClz != null) {
                    modelFields.put(field.getName(), fClz);
                }
            }
        }
        return this;
    }

    @Override
    public List<String> generateTestMethod() {
        List<String> testMethods = super.generateTestMethod();
        if (testMethods.size() == 0) {
            testMethods.add(GenerateTextUtils.getTestMethod("parseFromJSONObject", instance + ".parseFromJSONObject(jo);\n"));
        }
        for (int i = 0; i < testMethods.size(); i++) {
            String methodText = testMethods.get(i);
            //生成model测试代码
            Iterator iterator = modelFields.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PsiClass> entry = (Map.Entry<String, PsiClass>) iterator.next();
                String fVar = entry.getKey();
                PsiClass fClz = entry.getValue();
                //解析fClz,并生成对fVar的赋值和断言
                String[] assignAndAssert = _parseFieldAssignAndAssert(fVar, fClz);
                if (assignAndAssert.length != 2) {
                    continue;
                }
                //分别将赋值和断言添加到方法体的顶部和底部
                methodText = methodText.replace("{\n", "{\n" + assignAndAssert[0]).replace("}\n", assignAndAssert[1] + "}\n");
                testMethods.set(i, methodText);
            }
        }
        return testMethods;
    }

    //参照JSONModelTestGenerator算法解析成员变量的类并对其进行赋值和assert构建返回两部分的代码
    private String[] _parseFieldAssignAndAssert(String fVar, PsiClass clz) {
        String[] assignAndAssert = new String[2];
        //判断子类型是否可以解析
        boolean isAnalysable = false;
        for (PsiClass interfaces : clz.getInterfaces()) {
            if (interfaces.getName().equals("IParseFromJSONObject")) {
                isAnalysable = true;
                break;
            }
        }
        if (!isAnalysable) {
            return assignAndAssert;
        }
        //解析变量
        StringBuilder sb = new StringBuilder();
        PsiMethod method = null;
        HashMap<String, PsiField> allFields = new HashMap<>();
        for (PsiMethod m : clz.getMethods()) {
            if (m.getName().equals("parseFromJSONObject")) {
                method = m;
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
        //解析parseFromJSONObject方法，筛选有效变量
        HashMap<String, String> validExpressions = new LinkedHashMap<>();
        sb.setLength(0);
        if (method == null) {
            throw new IllegalArgumentException("111111");
        }
        if (method.getBody() == null) {
            throw new IllegalArgumentException("222222");
        }
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
            return assignAndAssert;
        }

        //TODO 生成测试变量并赋值
        // 为fVar的变量赋值
        String s;
        for (String var : validExpressions.keySet()) {
            PsiField field = allFields.get(var);
            s = String.format("%s %s = %s;\n", field.getType().getCanonicalText(), var, RandomValue.getRandomValueByType(field.getType()));
            sb.append(s);
        }
        //TODO 构建jsonObj
        sb.append("org.json.JSONObject jo = new org.json.JSONObject();\n");
        for (String expression : validExpressions.values()) {
            int start = 0;
            int end = expression.indexOf("(");
            if (end > start) {
                String tem = expression.substring(start, end);
                sb.append(expression.replace(tem, " jo.put"));
            }
        }
        assignAndAssert[0] = sb.toString();

        //TODO 生成Assert语句
        sb.setLength(0);
        String right = String.format("%s.%s.", instance, fVar);
        for (String var : validExpressions.keySet()) {
            sb.append(GenerateTextUtils.getAssertExpression(var, right + var));
        }
        assignAndAssert[1] = sb.toString();
        return assignAndAssert;
    }
}
