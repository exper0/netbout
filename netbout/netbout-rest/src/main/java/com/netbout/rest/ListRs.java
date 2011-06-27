/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email: privacy@netbout.com.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest;

// bout manipulation engine from com.netbout:netbout-engine
import com.netbout.engine.BoutFactory;
import com.netbout.engine.impl.DefaultBoutFactory;

// JAXB implemented data manipulators
import com.netbout.rest.jaxb.PageWithBouts;

// for JAX-RS
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Collection of Bouts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/")
public final class ListRs extends AbstractRs {

    /**
     * Public ctor.
     */
    public ListRs() {
        super();
    }

    /**
     * Ctor for unit testing.
     * @param builder The factory builder
     */
    protected ListRs(final FactoryBuilder builder) {
        super(builder);
    }

    /**
     * Get list of bouts.
     * @param query The query
     * @return The collection of bouts, to be converted into XML
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public PageWithBouts list(@DefaultValue("") @QueryParam("q")
        final String query) {
        return new PageWithBouts(this.builder().getBoutFactory(), query);
    }

    /**
     * Get one single bout as JAX-RS resource.
     * @param bout ID of the bout
     * @return The resource
     */
    @GET
    @Path("{id: \\d+}")
    public BoutRs bout(@PathParam("id") final Long bout) {
        return new BoutRs(this.builder(), bout);
    }

}
