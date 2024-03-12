package com.fern.util.clf;

import java.nio.file.Path;

/**
 * Instances of this class provide parsing for CLF.
 *
 * @see FileReadoutHandler
 * @see CLF
 * @see CLFParser
 */
public class CLFReadoutHandler extends FileReadoutHandler<CLF> {

    /**
     * Constructor
     * @param file file to be readout
     */
    public CLFReadoutHandler(Path file) {
        super(file);
    }

    @Override
    public CLF parseLine(String line) {
        return CLFParser.parseLogLine(line);
    }
}
