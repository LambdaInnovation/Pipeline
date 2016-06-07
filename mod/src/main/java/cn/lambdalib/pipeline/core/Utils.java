package cn.lambdalib.pipeline.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common unordered utils.
 */
public class Utils {

    public static final Logger log = LogManager.getLogger("Pipeline");

    public static RuntimeException notImplemented() {
        throw new RuntimeException("Method not implemented");
    }

    private Utils() {}

}
