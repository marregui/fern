package com.fern.chorradas;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class FindDoubleSingularity {
    static final double randomDouble(int significantDecimals) {
        StringBuilder sb = new StringBuilder("0.");
        for (int j = 0; j < significantDecimals; j++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return Double.parseDouble(sb.toString());
    }

    static final double toWireFromWire(double dbl, int significantDecimals) {
        return Double.parseDouble(String.format(String.format("%%.%df", significantDecimals), dbl));
    }

    static final boolean testEquality(double... dbls) {
        boolean equality = true;
        for (int i = 0; i < dbls.length; i++) {
            Double di = Double.valueOf(dbls[i]);
            equality &= di.equals(di);
            for (int j = 0; j < dbls.length; j++) {
                if (i != j) {
                    Double dj = Double.valueOf(dbls[j]);
                    equality &= di.equals(dj) && dj.equals(di);
                    if (!equality) {
                        break;
                    }
                }
            }
        }
        return equality;
    }

    static final void reportRuntime() {
        Properties props = System.getProperties();
        System.out.println("Runtime:");
        for (String key : new String[]{"java.vm.vendor", "java.vm.version", "java.runtime.version", "os.name",
                "os.version", "os.arch", "sun.arch.data.model"}) {
            System.out.printf(" - %s: %s\n", key, props.getProperty(key, "UNKNOWN"));
        }
    }

    public static void main(String[] args) throws Exception {
        reportRuntime();
        int significantDecimals = 1;
        while (true) {
            double dbl = randomDouble(significantDecimals);
            double dblFmt = Double.parseDouble(String.format("%f", dbl));
            double dblFmtToSD = toWireFromWire(dbl, significantDecimals);
            if (!testEquality(dbl, dblFmt, dblFmtToSD)) {
                System.out.printf("Breaks at '%d' significant decimals\n", significantDecimals);
                break;
            }
            significantDecimals++;
        }
    }
}