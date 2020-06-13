package run.halo.app.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @program: halo
 * @description:
 * @author: coortop
 * @create: 2020-06-12 23:49
 **/
public class ListUtils {

    private ListUtils(){
    }

    /**
     * 打乱list里的顺序
     * @param list
     * @return
     */
    public static List listDisruption(List list) {
        int size = list.size();
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            int randomPos = random.nextInt(size);
            Collections.swap(list, i, randomPos);
        }

        return list;
    }
}
