package eu.spaziodati.batchrefine.transformer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import javax.activation.MimeType;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import org.apache.commons.lang3.tuple.ImmutablePair;

import eu.fusepool.p3.transformer.commons.Entity;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * {@link SynchronousTransformer} is the synchronous Fusepool P3
 * {@link Transformer} for BatchRefine.
 * 
 * @author giuliano
 */
public class SynchronousTransformer extends BatchRefineTransformer implements
        SyncTransformer {

	private ITransformEngine fRefineEngine;

	public SynchronousTransformer(URI refineURI) {
		fRefineEngine = new RefineHTTPClient(refineURI);
	}

	@Override
	public boolean isLongRunning() {
		// XXX one issue is that the refine transformer is not *always*
		// long running.
		return false;
	}

	@Override
	public Entity transform(HttpRequestEntity entity) throws IOException {
		final HttpRequestEntity request = cast(entity);

		final ImmutablePair<MimeType, Properties> options = exporterOptions(request
				.getRequest());
		final File input = downloadInput(entity);
		final ITransformEngine engine = getEngine();

		return new WritingEntity() {
			@Override
			public void writeData(OutputStream out) throws IOException {
				try {
					engine.transform(input, fetchTransform(request), out,
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
			throw new IOException("BatchRefineTransformer requires a "
					+ HttpRequestEntity.class.getName());
		}

		return (HttpRequestEntity) entity;
	}

	private ITransformEngine getEngine() {
		return fRefineEngine;
	}

}
