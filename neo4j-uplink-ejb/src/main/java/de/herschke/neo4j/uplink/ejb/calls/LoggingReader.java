/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.herschke.neo4j.uplink.ejb.calls;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author rhk
 */
public class LoggingReader extends Reader {

    private final Reader reader;

    public LoggingReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = reader.read(cbuf, off, len);
        if (read >= 0) {
            System.out.print(new String(cbuf, off, read));
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
