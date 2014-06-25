package eu.spaziodati.batchrefine.extractor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import javax.activation.MimeType;

import org.apache.commons.lang3.tuple.ImmutablePair;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.Extractor;
import eu.fusepool.extractor.HttpRequestEntity;
import eu.fusepool.extractor.SyncExtractor;
import eu.fusepool.extractor.util.WritingEntity;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * {@link SynchronousTransformer} is the synchronous Fusepool P3
 * {@link Extractor} for BatchRefine.
 * 
 * @author giuliano
 */
public class SynchronousTransformer extends BatchRefineTransformer implements
		SyncExtractor {

	private ITransformEngine fRefineEngine;

	public SynchronousTransformer(URI refineURI) {
		fRefineEngine = new RefineHTTPClient(refineURI);
	}

	@Override
	public boolean isLongRunning() {
		// XXX one issue is that the refine extractor is not *always*
		// long running.
		return false;
	}

	@Override
	public Entity extract(HttpRequestEntity entity) throws IOException {
		final HttpRequestEntity request = cast(entity);

		final ImmutablePair<MimeType, Properties> options = exporterOptions(request
				.getRequest());
		final File input = downloadInput(entity);
		final ITransformEngine engine = getEngine();

		return new WritingEntity() {
			@Override
			public void writeData(OutputStream out) throws IOException {
				try {
					engine.transform(input, transform(request), out,
							options.right);
				} finally {
					input.delete();
				}
			}

			@Override
			public MimeType getType() {
				return options.left;
			}
		};
	}

	private HttpRequestEntity cast(Entity entity) throws IOException {
		// TODO have to discuss with Reto how to properly:
		// 1 - handle/report error conditions (for now I just throw
		// IOException);
		// 2 - properly get the parameters from the enclosing HTTP request.
		if (!(entity instanceof HttpRequestEntity)) {
			throw new IOException("BatchRefineExtractor requires a "
					+ HttpRequestEntity.class.getName());
		}

		return (HttpRequestEntity) entity;
	}

	private ITransformEngine getEngine() {
		return fRefineEngine;
	}

}
