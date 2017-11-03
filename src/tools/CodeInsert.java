package tools;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.apache.http.util.TextUtils;

/**
 * Created by shijiecui on 2017/11/1.
 */
public class CodeInsert {
    private PsiElementFactory factory;
    private JavaCodeStyleManager styleManager;
    private PsiFile target;
    private PsiClass clz;
    private static CodeInsert insert = new CodeInsert();

    public static CodeInsert getInstance(PsiFile target) {
        insert._updateTools(target);
        return insert;
    }

    private void _updateTools(PsiFile target) {
        if (target != null && !target.equals(this.target)) {
            this.target = target;
            for (PsiElement e : target.getChildren()) {
                if (e instanceof PsiClass) {
                    this.clz = (PsiClass) e;
                    break;
                }
            }
            factory = JavaPsiFacade.getElementFactory(target.getProject());
            styleManager = JavaCodeStyleManager.getInstance(target.getProject());
        }
    }

    public PsiElement insertComment(String comment, PsiElement commentOwner) {
        if (clz != null && !TextUtils.isEmpty(comment)) {
            PsiElement startElement = target.findElementAt(commentOwner.getStartOffsetInParent());
            PsiComment _comment = factory.createCommentFromText("//" + comment, null);
            try {
                return styleManager.shortenClassReferences(clz.addBefore(_comment, startElement));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public PsiElement insertPackageReference(String packageStatement) {
        if (target != null && clz != null) {
            PsiPackageStatement _package = factory.createPackageStatement(packageStatement);
            try {
                return styleManager.shortenClassReferences(clz.addBefore(_package, clz));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public PsiElement insertFiled(String filed) {
        if (!TextUtils.isEmpty(filed) && target != null && clz != null) {
            PsiField _field = factory.createFieldFromText(filed, clz);
            styleManager.optimizeImports(target);
            try {
                return styleManager.shortenClassReferences(clz.addBefore(_field, clz.getLastChild()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public PsiElement insertMethod(String method) {
        if (!TextUtils.isEmpty(method) && target != null && clz != null) {
            PsiMethod _method = factory.createMethodFromText(method, clz);
            styleManager.optimizeImports(target);
            try {
                return styleManager.shortenClassReferences(clz.addBefore(_method, clz.getLastChild()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public PsiElement checkSetClassModifier(String modifier) {
        if (clz != null && !TextUtils.isEmpty(modifier) && !clz.getModifierList().hasModifierProperty(modifier)) {
            PsiModifierList modifierList = factory.createFieldFromText(modifier + " int a;", null).getModifierList();
            PsiElement startElement = target.findElementAt(clz.getTextOffset() - 6);
            try {
                return styleManager.shortenClassReferences(clz.addBefore(modifierList, startElement));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
