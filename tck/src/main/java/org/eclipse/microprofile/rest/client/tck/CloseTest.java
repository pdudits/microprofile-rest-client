/*
 * Copyright 2019 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.tck;

import java.net.URI;
import java.io.Closeable;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.StringClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith200RequestFilter;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class CloseTest extends Arquillian{
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, CloseTest.class.getSimpleName()+".war")
            .addClasses(ReturnWith200RequestFilter.class, StringClient.class);
    }

    @Test(expectedExceptions={IllegalStateException.class})
    public void expectIllegalStateExceptionAfterCloseableClose() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(ReturnWith200RequestFilter.class);
        StringClient client = builder.baseUri(new URI("http://localhost/stub")).build(StringClient.class);
        try {
            // ensure client works correctly before closing
            assertEquals(client.getHeaderValue("foo"), "OK");
        }
        catch(Throwable t) {
            fail("Initial (unclosed) request threw unexpected exception", t);
        }

        try {
            ((Closeable) client).close();
        }
        catch (Throwable t) {
            fail("Caught unexpected exception closing client", t);
        }

        client.getHeaderValue("IllegalStateException expected");
    }

    @Test(expectedExceptions={IllegalStateException.class})
    public void expectIllegalStateExceptionAfterAutoCloseableClose() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(ReturnWith200RequestFilter.class);
        StringClient client = builder.baseUri(new URI("http://localhost/stub")).build(StringClient.class);
        try ( AutoCloseable ac = (AutoCloseable) client; ) {
            assertEquals(client.getHeaderValue("foo"), "OK");
        }
        catch(Throwable t) {
            fail("Initial (unclosed) request or attempt to close threw unexpected exception", t);
        }

        client.getHeaderValue("IllegalStateException expected");
    }
}
