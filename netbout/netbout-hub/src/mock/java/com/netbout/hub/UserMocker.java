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
package com.netbout.hub;

import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import java.util.Random;
import org.mockito.Mockito;

/**
 * Mocker of {@link User}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class UserMocker {

    /**
     * The object.
     */
    private final transient User user = Mockito.mock(User.class);

    /**
     * Public ctor.
     */
    public UserMocker() {
        final String name = String.format(
            "Mark no.%d",
            Math.abs(new Random().nextInt())
        );
        Mockito.doReturn(name).when(this.user).name();
    }

    /**
     * With this name.
     * @param name The name of it
     * @return This object
     */
    public UserMocker namedAs(final String name) {
        Mockito.doReturn(name).when(this.user).name();
        return this;
    }

    /**
     * With this identity on board.
     * @param name Name of identity
     * @param identity The identity
     * @return This object
     * @throws Exception If any problem
     */
    public UserMocker withIdentity(final String name, final Identity identity)
        throws Exception {
        Mockito.doReturn(identity).when(this.user).identity(name);
        return this;
    }

    /**
     * With this identity on board.
     * @param name The name of it
     * @return This object
     * @throws Exception If any problem
     */
    public UserMocker withIdentity(final String name) throws Exception {
        final Identity identity = new IdentityMocker().namedAs(name).mock();
        Mockito.doReturn(identity).when(this.user).identity(name);
        return this;
    }

    /**
     * Build it.
     * @return The bout
     */
    public User mock() {
        return this.user;
    }

}