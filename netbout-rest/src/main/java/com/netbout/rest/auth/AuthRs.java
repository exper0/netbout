/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
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
package com.netbout.rest.auth;

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.netbout.client.RestSession;
import com.netbout.rest.BaseRs;
import com.netbout.rest.CryptedIdentity;
import com.netbout.rest.LoginRequiredException;
import com.netbout.rest.NbPage;
import com.netbout.spi.Identity;
import com.netbout.spi.text.SecureString;
import com.rexsl.page.CookieBuilder;
import com.rexsl.page.PageBuilder;
import java.net.URI;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * REST authentication page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/auth")
public final class AuthRs extends BaseRs {

    /**
     * The URL to go next.
     */
    private transient URI forward;

    /**
     * It's a super-user mode.
     */
    private transient boolean sudo;

    /**
     * Set goto URI.
     * @param uri The URI
     */
    @CookieParam(RestSession.GOTO_COOKIE)
    public void setGoto(final String uri) {
        if (uri != null) {
            try {
                this.forward = new URI(SecureString.valueOf(uri).text());
            } catch (com.netbout.spi.text.StringDecryptionException ex) {
                Logger.warn(
                    this,
                    "#setGoto('%s'): failed to decrypt: %[exception]s",
                    uri,
                    ex
                );
            } catch (java.net.URISyntaxException ex) {
                Logger.warn(
                    this,
                    "#setGoto('%s'): failed to create URI: %[exception]s",
                    uri,
                    ex
                );
            }
        }
    }

    /**
     * Set SUDO mode.
     * @param secret Secret code of a super user
     */
    @QueryParam(RestSession.SUDO_PARAM)
    public void setSudo(final String secret) {
        if (secret != null) {
            if (secret.equals(Manifests.read("Netbout-SuperSecret"))) {
                this.sudo = true;
            } else {
                throw new IllegalArgumentException(
                    String.format(
                        "wrong secret code of a super user: '%s'",
                        secret
                    )
                );
            }
        }
    }

    /**
     * Authentication page.
     * @param iname Identity name
     * @param secret Secret word
     * @param path Where to go next
     * @return The JAX-RS response
     * @todo #158 Path annotation: http://java.net/jira/browse/JERSEY-739
     */
    @GET
    @Path("/")
    public Response auth(@QueryParam("identity") final URN iname,
        @QueryParam("secret") final String secret,
        @QueryParam("goto") @DefaultValue("/") final String path) {
        if (iname == null) {
            throw new LoginRequiredException(
                this,
                "'identity' query param is mandatory"
            );
        }
        Identity identity;
        if (this.sudo) {
            identity = this.bypass(iname);
        } else {
            if (iname == null) {
                throw new LoginRequiredException(
                    this,
                    "'secret' query param is mandatory"
                );
            }
            identity = this.identity(iname, secret);
        }
        URI location;
        if (this.forward == null) {
            location = this.base().path(path).build();
        } else {
            location = this.forward;
        }
        return new PageBuilder()
            .build(NbPage.class)
            .init(this)
            .authenticated(identity)
            .cookie(
                new CookieBuilder(this.base())
                    .name(RestSession.GOTO_COOKIE)
                    .build()
            )
            .status(Response.Status.SEE_OTHER)
            .location(location)
            .header(RestSession.AUTH_HEADER, new CryptedIdentity(identity))
            .build();
    }

    /**
     * Create identity.
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity
     */
    private Identity identity(final URN iname, final String secret) {
        Identity identity;
        try {
            final Identity previous = this.identity();
            this.logoff();
            identity = this.authenticate(iname, secret);
            if (NbPage.trusted(identity)
                && !NbPage.trusted(previous)) {
                identity = this.hub().join(identity, previous);
            } else if (NbPage.trusted(previous)
                && !NbPage.trusted(identity)) {
                identity = this.hub().join(previous, identity);
            } else if (identity.name().equals(previous.name())) {
                Logger.info(
                    this,
                    "Successfull re-authentication of '%s'",
                    identity.name()
                );
            } else {
                Logger.info(
                    this,
                    "Authentication of '%s' was replaced by '%s'",
                    previous.name(),
                    identity.name()
                );
            }
        } catch (LoginRequiredException ex) {
            identity = this.authenticate(iname, secret);
        }
        return identity;
    }

    /**
     * Authenticate the user through facebook.
     * @param iname Identity name
     * @param secret Secret word
     * @return The identity found
     */
    private Identity authenticate(final URN iname, final String secret) {
        RemoteIdentity remote;
        try {
            remote = new AuthMediator(this.hub().resolver())
                .authenticate(iname, secret);
        } catch (java.io.IOException ex) {
            Logger.warn(this, "%[exception]s", ex);
            throw new LoginRequiredException(this, ex);
        }
        Identity identity;
        try {
            identity = remote.findIn(this.hub());
        } catch (Identity.UnreachableURNException ex) {
            throw new LoginRequiredException(this, ex);
        }
        return identity;
    }

    /**
     * Bypass authentication and return an identity.
     * @param iname Identity name
     * @return Identity
     */
    private Identity bypass(final URN iname) {
        try {
            return this.hub().identity(iname);
        } catch (Identity.UnreachableURNException ex) {
            Logger.warn(this, "sudo %[exception]s", ex);
            throw new LoginRequiredException(this, ex);
        }
    }

}