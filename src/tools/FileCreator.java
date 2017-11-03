package tools;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

import java.util.StringTokenizer;

/**
 * Created by shijiecui on 2017/11/1.
 */
public class FileCreator {
    public static final String TAG = "FileCreator";

    public static PsiFile checkAndCreateTestFile(PsiDirectory directory, PsiClass clz) {
        PsiFile target = null;
        final String targetName = getTestClassFileName(clz);
        if (directory != null && clz != null && targetName != null) {
            PsiDirectory dir = _getTargetDirectory(directory);

            target = dir.findFile(targetName);
            if (target == null) {
                PsiClass psiClz = _createJavaClass(dir, getTestClassName(clz));
                if (psiClz != null) {
                    target = dir.findFile(targetName);
                }
            }
        }
        return target;
    }

    public static String getTestClassName(PsiClass clz) {
        if (clz != null) {
            return clz.getName() + "Test";
        }
        return null;
    }

    public static String getTestClassFileName(PsiClass clz) {
        if (clz != null) {
            return getTestClassName(clz) + ".java";
        }
        return null;
    }

    private static PsiClass _createJavaClass(PsiDirectory directory, String fileName) {
        return JavaDirectoryService.getInstance().createClass(directory, fileName);
    }

    // createDirectory()
    public static PsiDirectory createDirectory(PsiDirectory parent, String name)
            throws IncorrectOperationException {
        PsiDirectory result = null;

        for (PsiDirectory dir : parent.getSubdirectories()) {
            if (dir.getName().equalsIgnoreCase(name)) {
                result = dir;
                break;
            }
        }

        if (null == result) {
            result = parent.createSubdirectory(name);
        }

        return result;
    }

    // createPackage()
    public static PsiDirectory createPackage(PsiDirectory sourceDir, String qualifiedPackage)
            throws IncorrectOperationException {
        PsiDirectory parent = sourceDir;
        StringTokenizer token = new StringTokenizer(qualifiedPackage, ".");
        while (token.hasMoreTokens()) {
            String dirName = token.nextToken();
            parent = createDirectory(parent, dirName);
        }
        return parent;
    }

    private static PsiDirectory _getTargetDirectory(PsiDirectory directory) {
        String path = directory.getVirtualFile().getPath();
        if (path.contains("androidTest")) {
            throw new IllegalAccessError("must choose a java file from main directory in your android project!");
        }
        int index = path.indexOf("/main/");
        if (index == -1) {
            throw new IllegalAccessError("must choose a java file from main directory in your android project!");
        }
        String targetPath = path.substring(index).replace("/main", "test");
        String[] paths = targetPath.split("/");
        PsiDirectory dir = directory;
        while (dir != null && !dir.getName().equals("src")) {
            dir = dir.getParent();
        }
        if (dir != null && dir.getName().equals("src")) {
            index = 0;
            while (index < paths.length) {
                PsiDirectory d = dir.findSubdirectory(paths[index]);
                if (d == null) {
                    d = createDirectory(dir, paths[index]);
                }
                index++;
                dir = d;
            }
        }
        return dir;
    }

    public static void openFileInIde(PsiFile file) {
        new OpenFileDescriptor(file.getProject(), file.getVirtualFile()).navigate(true);
    }
}
