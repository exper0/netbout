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
package com.netbout.inf.predicates;

import com.netbout.bus.Bus;
import com.netbout.inf.Msg;
import com.netbout.inf.Predicate;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Call predicate by name in Hub.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class CustomPred extends AbstractVarargPred {

    /**
     * Bus to work with.
     */
    private final transient Bus ibus;

    /**
     * Public ctor.
     * @param bus The bus to work with
     * @param name Name of the predicate
     * @param args The arguments
     */
    public CustomPred(final Bus bus, final Urn name,
        final List<Predicate> args) {
        super(name.toString(), args);
        this.ibus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final Msg msg, final int pos) {
        final List<Object> values = new ArrayList<Object>();
        for (Predicate pred : this.args()) {
            values.add(pred.evaluate(msg, pos));
        }
        final Object result = this.ibus.make("evaluate-predicate")
            .arg(msg.bout())
            .arg(msg.number())
            .arg(Urn.create(this.name()))
            .arg(values)
            .asDefault(false)
            .exec();
        Logger.debug(
            this,
            "#evaluate(): evaluated '%s': %[type]s",
            this.name(),
            result
        );
        return result;
    }

}
