/**
 * Copyright (c) 2009-2014, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
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
package com.netbout.rest.bout;

import com.netbout.spi.Bout;
import com.netbout.spi.Friends;
import com.netbout.spi.User;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;

/**
 * Invite a friend to the bout.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.14
 */
final class TkInvite implements Take {

    /**
     * User.
     */
    private final transient User user;

    /**
     * Bout.
     */
    private final transient Bout bout;

    /**
     * Ctor.
     * @param bot Bout
     */
    TkInvite(final User usr, final Bout bot) {
        this.user = usr;
        this.bout = bot;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String name = new RqForm(req).param("name").iterator().next();
        final String check = this.user.aliases().check(name);
        if (check.isEmpty()) {
            throw new RsForward(
                new RsFlash(
                    String.format("incorrect alias '%s', try again", name),
                    Level.WARNING

                )
            );
        }
        try {
            this.bout.friends().invite(name);
        } catch (final Friends.UnknownAliasException ex) {
            throw new RsForward(new RsFlash(ex));
        }
        return new RsForward(
            new RsFlash(
                String.format(
                    "new person invited to the bout #%d",
                    this.bout.number()
                ),
                Level.INFO
            )
        );
    }

}