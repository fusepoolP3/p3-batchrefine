package eu.spaziodati.batchrefine.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * {@link CloseableWrappingStream} is an {@link InputStream} wrapper which takes
 * a set of accompanying {@link Closeable} objects that should be closed when
 * this stream gets closed.
 * 
 * @author giuliano
 */
public class CloseableWrappingStream extends InputStream {

	private final Logger fLogger = Logger
			.getLogger(CloseableWrappingStream.class);

	private final InputStream fDelegate;

	private final List<Closeable> fCloseables = Collections
			.synchronizedList(new ArrayList<Closeable>());

	public CloseableWrappingStream(InputStream delegate,
			Closeable... closeables) {
		fDelegate = delegate;
		fCloseables.add(fDelegate);
		for (Closeable closeable : closeables) {
			fCloseables.add(closeable);
		}
	}

	public CloseableWrappingStream closeWith(Closeable closeable) {
		fCloseables.add(closeable);
		return this;
	}

	@Override
	public int read() throws IOException {
		return fDelegate.read();
	}

	@Override
	public void close() throws IOException {
		Iterator<Closeable> it = fCloseables.iterator();
		while (it.hasNext()) {
			try {
				it.next().close();
			} catch (Exception ex) {
				fLogger.error(ex);
			}
			it.remove();
		}
	}
}
