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
package com.netbout.inf.functors;

import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import com.netbout.inf.Atom;
import com.netbout.inf.FolderMocker;
import com.netbout.inf.MsgMocker;
import com.netbout.inf.Ray;
import com.netbout.inf.Term;
import com.netbout.inf.notices.JoinNotice;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.inf.ray.MemRay;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Identity;
import com.netbout.spi.IdentityMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link Bundled}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class BundledTest {

    /**
     * Bundled can find bundled messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsBundledMessages() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final long msg = MsgMocker.number();
        final Bout bout = new BoutMocker().mock();
        final Bundled functor = new Bundled();
        for (int num = 0; num < 2; ++num) {
            final long number = msg - num;
            ray.msg(number);
            functor.see(ray, BundledTest.notice(number, bout));
        }
        final Term term = new Bundled().build(
            ray,
            Arrays.asList(new Atom<?>[0])
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).msg().number(),
            Matchers.equalTo(msg)
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).end(),
            Matchers.equalTo(true)
        );
    }

    /**
     * Bundled can change bundle marker on join and kickoff.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsBundledMessagesAfterJoinAndKickoff() throws Exception {
        final Ray ray = new MemRay(new FolderMocker().mock().path());
        final URN dude = new URNMocker().mock();
        final Bout first = new BoutMocker()
            .withParticipant(dude)
            .mock();
        final long fmsg = MsgMocker.number();
        final Bout second = new BoutMocker()
            .withParticipant(dude)
            .mock();
        final long smsg = fmsg + 1;
        final Bundled functor = new Bundled();
        final Equal equal = new Equal();
        ray.msg(fmsg);
        equal.see(ray, BundledTest.notice(fmsg, first));
        functor.see(ray, BundledTest.notice(fmsg, first));
        ray.msg(smsg);
        equal.see(ray, BundledTest.notice(smsg, second));
        functor.see(ray, BundledTest.notice(smsg, second));
        functor.see(
            ray,
            new JoinNotice() {
                @Override
                public Bout bout() {
                    return new BoutMocker()
                        .withNumber(first.number())
                        .withParticipant(new URNMocker().mock())
                        .mock();
                }
                @Override
                public Identity identity() {
                    return new IdentityMocker().mock();
                }
            }
        );
        final Term term = new Bundled().build(
            ray,
            Arrays.asList(new Atom<?>[0])
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).msg().number(),
            Matchers.equalTo(smsg)
        );
        MatcherAssert.assertThat(
            ray.cursor().shift(term).msg().number(),
            Matchers.equalTo(fmsg)
        );
    }

    /**
     * Create a message posted notice.
     * @param num Number of it
     * @param bout Bout it belongs to
     * @return The notice
     */
    private static MessagePostedNotice notice(final long num, final Bout bout) {
        return new MessagePostedNotice() {
            @Override
            public Message message() {
                return new MessageMocker()
                    .withNumber(num)
                    .mock();
            }
            @Override
            public Bout bout() {
                return bout;
            }
        };
    }

}