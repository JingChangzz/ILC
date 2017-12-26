package ilc.data;

import java.io.IOException;
import java.util.Set;

public interface IPermissionMethodParser {
    Set<AndroidMethod> parse() throws IOException;
}
