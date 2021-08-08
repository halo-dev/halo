package run.halo.app.utils.flexmark.ext.math.internal;

import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSetter;
import org.jetbrains.annotations.NotNull;
import run.halo.app.utils.flexmark.ext.math.MathExtension;

public class MathOptions implements MutableDataSetter {
    public final String inlineMathClass;

    public final String blockMathClass;

    public final boolean nestedBlockMath;

    public MathOptions(DataHolder options) {
        this.inlineMathClass = (String) MathExtension.INLINE_MATH_CLASS.get(options);
        this.blockMathClass = (String) MathExtension.BLOCK_MATH_CLASS.get(options);
        this.nestedBlockMath = (Boolean) MathExtension.NESTED_BLOCK_MATH.get(options);
    }

    public @NotNull MutableDataHolder setIn(MutableDataHolder mutableDataHolder) {
        mutableDataHolder.set(MathExtension.INLINE_MATH_CLASS, this.inlineMathClass);
        mutableDataHolder.set(MathExtension.BLOCK_MATH_CLASS, this.blockMathClass);
        mutableDataHolder.set(MathExtension.NESTED_BLOCK_MATH, this.nestedBlockMath);
        return mutableDataHolder;
    }
}
