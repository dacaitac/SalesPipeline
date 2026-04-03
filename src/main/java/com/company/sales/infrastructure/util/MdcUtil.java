package com.company.sales.infrastructure.util;

import org.slf4j.MDC;
import java.util.Map;
import java.util.function.Supplier;

public class MdcUtil {

    private MdcUtil() {
        // Prevenir instanciación
    }

    /**
     * Propaga el contexto MDC actual al hilo secundario que ejecuta el Supplier.
     */
    public static <T> Supplier<T> wrapWithMdc(Supplier<T> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            } else {
                MDC.clear();
            }
            try {
                return supplier.get();
            } finally {
                MDC.clear();
            }
        };
    }
}