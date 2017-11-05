package tools;

import com.intellij.psi.PsiType;
import org.apache.commons.lang.RandomStringUtils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Created by shijiecui on 2017/11/2.
 */
public class RandomValue {
    private static Random random = new Random();

    public static int getRandomInt() {
        return random.nextInt(2017);
    }

    public static long getRandomLong() {
        long value = random.nextLong();
        if (value < 0) {
            value = -value;
        }
        return value;
    }

    public static String getRandomString() {
        int len = 0;
        while (len < 4) {
            len = random.nextInt(10);
        }
        return RandomStringUtils.randomAlphanumeric(len);
    }

    public static boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    public static double getRandomDouble() {
        return getRandomFloat();
    }

    public static float getRandomFloat() {
        Float d = random.nextFloat();
        if (d < 0) {
            d = -d;
        }
        BigDecimal b = new BigDecimal(d * 100);
        //参数说明2代表保留2为小数      BigDecimal.ROUND_HALF_UP四舍五入
        float x = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return x;
    }

    public static String getRandomValueByType(PsiType type) {
        String _type = type.getCanonicalText();
        switch (_type) {
            case "java.lang.String":
                return "\"" + getRandomString() + "\"";
            case "int":
                return getRandomInt() + "";
            case "long":
                return getRandomLong() + "L";
            case "boolean":
                return getRandomBoolean() + "";
            case "float":
                return getRandomFloat() + "F";
            case "double":
                return getRandomDouble() + "D";
            default:
                if (_type.contains("List")) {
                    return "new java.util.ArrayList<>();";
                }
                return "null";
        }
    }
}