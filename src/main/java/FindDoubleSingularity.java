/**
 * Copyright (c) Miguel Arregui. All rights reserved.
 * 
 * The use and distribution terms for this software are covered by the
 * 
 * Apache License 2.0
 * (https://opensource.org/licenses/Apache-2.0)
 * 
 * available in the LICENSE file at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound
 * by the terms of this license. You must not remove this notice, or
 * any other, from this software.
 **/
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class FindDoubleSingularity {
  static final double randomDouble(final int significantDecimals) {
    final StringBuilder sb = new StringBuilder("0.");
    for (int j = 0; j < significantDecimals; j++) {
      sb.append(ThreadLocalRandom.current().nextInt(10));
    }
    return Double.parseDouble(sb.toString());
  }

  static final double toWireFromWire(final double dbl, final int significantDecimals) {
    return Double.parseDouble(String.format(String.format("%%.%df", significantDecimals), dbl));
  }

  static final boolean testEquality(double... dbls) {
    boolean equality = true;
    for (int i = 0; i < dbls.length; i++) {
      final Double di = Double.valueOf(dbls[i]);
      equality &= di.equals(di);
      for (int j = 0; j < dbls.length; j++) {
        if (i != j) {
          final Double dj = Double.valueOf(dbls[j]);
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
    final Properties props = System.getProperties();
    System.out.println("Runtime:");
    for (String key : new String[] { "java.vm.vendor", "java.vm.version", "java.runtime.version", "os.name",
        "os.version", "os.arch", "sun.arch.data.model" }) {
      System.out.printf(" - %s: %s\n", key, props.getProperty(key, "UNKNOWN"));
    }
  }

  public static void main(String[] args) throws Exception {
    reportRuntime();
    int significantDecimals = 1;
    while (true) {
      final double dbl = randomDouble(significantDecimals);
      final double dblFmt = Double.parseDouble(String.format("%f", dbl));
      final double dblFmtToSD = toWireFromWire(dbl, significantDecimals);
      if (!testEquality(dbl, dblFmt, dblFmtToSD)) {
        System.out.printf("Breaks at '%d' significant decimals\n", significantDecimals);
        break;
      }
      significantDecimals++;
    }
  }
}