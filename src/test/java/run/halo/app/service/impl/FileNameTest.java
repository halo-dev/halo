package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test if file name is legal as without any reserved character
 *
 * @author eziosudo
 * @date 22-6-7
 */
@Slf4j
class FileNameTest {

    private static final Pattern FILE_PATTERN = Pattern.compile("[\\\\/:*?\"<>|]");

    @Test
    public void fileNameWithReservedCharsWillBeReplaced(){
        String fileName1 = "abcde";
        String filteredFileName1 = FILE_PATTERN.matcher(fileName1).replaceAll("") + ".md";
        assertEquals("abcde.md", filteredFileName1);

        String fileName2 = "abcde|字符替换\\星号*大于>小于<slash/中文字符、";
        String filteredFileName2 = FILE_PATTERN.matcher(fileName2).replaceAll("") + ".md";
        assertEquals("abcde字符替换星号大于小于slash中文字符、.md", filteredFileName2);
    }

}
