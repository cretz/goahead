package goahead.interop.builtin;

import goahead.interop.GoFunction;
import goahead.interop.GoType;

@GoType("string")
public class GoString {

    @GoFunction("[]")
    public native Byte get(Integer i);

    @GoFunction("[:]")
    public native String slice();

    @GoFunction("[:]")
    public native String slice(Integer low, Integer high);

    @GoFunction("[:]")
    public native String sliceFrom(Integer low);

    @GoFunction("[:]")
    public native String sliceUntil(Integer high);
}
