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
package com.netbout.inf;

import com.netbout.bus.Bus;
import com.netbout.spi.Identity;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The task to review one identity.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeIdentityTask extends AbstractTask {

    /**
     * The infinity.
     */
    private final transient Infinity infinity;

    /**
     * The bus.
     */
    private final transient Bus bus;

    /**
     * The identity.
     */
    private final transient Identity identity;

    /**
     * Public ctor.
     * @param inf The infinity
     * @param where The BUS to work with
     * @param what The identity to update
     * @param index The index to use
     * @checkstyle ParameterNumber (3 lines)
     */
    public SeeIdentityTask(final Infinity inf, final Bus where,
        final Identity what, final Index index) {
        super(index);
        this.infinity = inf;
        this.bus = where;
        this.identity = what;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        return new HashSet(Arrays.asList(new Urn[] {this.identity.name()}));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("see-identity-%s", this.identity.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() {
        final Collection<Long> numbers = new TreeSet<Long>(
            (List<Long>) this.bus.make("get-bouts-of-identity")
                .synchronously()
                .arg(this.identity.name())
                .asDefault(new ArrayList<Long>())
                .exec()
        );
        for (Long number : numbers) {
            try {
                this.infinity.see(this.identity.bout(number));
            } catch (com.netbout.spi.BoutNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Logger.debug(
            this,
            "#execute(): cached %d bout(s) of '%s' in %[nano]s",
            numbers.size(),
            this.identity.name(),
            this.time()
        );
    }

}
