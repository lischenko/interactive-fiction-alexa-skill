package ifas.zax;

import com.zaxsoft.zmachine.ZCPU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implements reflection hack to avoid changing ZAX code
 */
public class ZaxPublicMorozov {
    private static final Logger LOG = LoggerFactory.getLogger(ZaxPublicMorozov.class);

    public static void zop_save(ZCPU cpu, String dumpFilename, MyZaxUi ui) throws IOException {
        zcpuPersistenceReflectionHelper(cpu, ui, dumpFilename, "zop_save");
    }

    public static void zop_restore(ZCPU cpu, MyZaxUi ui, String dumpFilename) throws IOException {
        zcpuPersistenceReflectionHelper(cpu, ui, dumpFilename, "zop_restore");
    }

    private static void zcpuPersistenceReflectionHelper(ZCPU cpu, MyZaxUi ui, String dumpFilename, String method) {
        synchronized (ui) {
            // makes ZaxUI provide proper filename
            ui.setNextFilename(dumpFilename);
            zcpuReflectionCall(cpu, method);
        }
    }

    public static void zcpuReflectionCall(ZCPU cpu, String method) {
        try {
            Method m = ZCPU.class.getDeclaredMethod(method);
            m.setAccessible(true);

            m.invoke(cpu);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOG.error("Could not hack ZAX " + method, e);
        }
    }


    public static void zcpuReflectionFlag(ZCPU cpu, String flag, boolean b) {
        try {
            Field f = ZCPU.class.getDeclaredField(flag);
            f.setAccessible(true);

            f.setBoolean(cpu, b);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Could not hack ZAX flag " + flag, e);
        }
    }
}
