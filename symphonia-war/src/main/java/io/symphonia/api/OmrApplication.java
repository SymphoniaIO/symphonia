package io.symphonia.api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * A class extending {@link javax.ws.rs.core.Application} is the portable way to
 * define JAX-RS 2.0 resources, and the {@link javax.ws.rs.ApplicationPath}
 * defines the root path shared by all these resources.
 *
 * @author sbunciak
 */
@ApplicationPath("api")
public class OmrApplication extends Application {
}