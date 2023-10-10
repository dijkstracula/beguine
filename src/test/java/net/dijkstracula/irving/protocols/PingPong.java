package net.dijkstracula.irving.protocols;

import net.dijkstracula.irving.sorts.IvyClass;
import net.dijkstracula.irving.sorts.IvySort;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.irving.sorts.UnboundedSequence;
import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import java.lang.Void;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class PingPong {
    public class PP extends Protocol {
        final int ping_kind = 0;
        final int pong_kind = 1;

        final ReliableNetwork<msg_t> net;

        final Range pid;

        public PP(MelinaContext ctx) {
            super(ctx);
            net = new ReliableNetwork<>(ctx);
            pid = ctx.mkRange("pid", 0, 3);

            exportAction("net.recvf", net.recvf, ctx.randomSelect(net.sockets));

            List<IvyObj_proc> proc_instances = LongStream.range(0, 2).mapToObj(i -> new IvyObj_proc(i)).collect(Collectors.toList());
            proc = i -> proc_instances.get(i.intValue());
        }

        class msg_t {
            // Fields
            public long typ;
            public long src;
            public long dst;
            public long ping_val;

            // Actions
        }
        class Ivy_msg_t_Factory extends IvyClass<msg_t> {
            // Actions


            // Fields
            IvySort<Long, ?> typ;
            IvySort<Long, ?> src;
            IvySort<Long, ?> dst;
            IvySort<Long, ?> ping_val;


            public Ivy_msg_t_Factory(MelinaContext ctx) {
                super("msg_t", ctx);
                typ = ctx.mkRange("msg_kind", 0, 2);
                src = pid;
                dst = pid;
                ping_val = new UnboundedSequence(ctx);
            }

            @Override
            public msg_t make() { return new msg_t(); }

            @Override
            public Supplier<msg_t> generator() {
                Supplier<Long> typ_gen = typ.generator();
                Supplier<Long> src_gen = src.generator();
                Supplier<Long> dst_gen = dst.generator();
                Supplier<Long> ping_val_gen = ping_val.generator();
                return () -> {
                    msg_t ret = make();
                    ret.typ = typ_gen.get();
                    ret.src = src_gen.get();
                    ret.dst = dst_gen.get();
                    ret.ping_val = ping_val_gen.get();
                    return ret;
                };
            }
        }

        class IvyObj_proc {
            private Long self;

            ReliableNetwork<msg_t>.Socket sock;

            public IvyObj_proc(Long self) {
                this.self = self;

                sock = net.dial.apply(self);

                sock.recv.on((Long src, msg_t msg) -> {
                    System.out.println(String.format("[sock.recv] Received %d on node %d", msg.ping_val, self));
                    if (msg.typ == ping_kind) {
                        msg.typ = pong_kind;
                    } else {
                        msg.typ = ping_kind;
                    }
                    ;
                    msg.dst = ((msg.src) >= 1 ? 1 : ((msg.src) < 0 ? 0 : (msg.src)));
                    msg.src = ((self) >= 1 ? 1 : ((self) < 0 ? 0 : (self)));
                    msg.ping_val += 1;
                    sock.send.apply(proc.apply(((msg.dst) >= 1 ? 1 : ((msg.dst) < 0 ? 0 : (msg.dst)))).sock.id, msg);
                    return null;
                });

            } //cstr
        }

        Function<Long, IvyObj_proc> proc;

        // XXX: this was hand-written, feels like this ought to be unnecessary with a better interface...
        public msg_t initial() {
            return new msg_t();
        }
    }

    @Test
    void PingPongTest() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        PP pingpong = new PP(ctx);

        PP.msg_t initial_msg = pingpong.initial();
        initial_msg.typ = pingpong.ping_kind;
        initial_msg.src = 0;
        initial_msg.dst = 1;
        initial_msg.ping_val = 42;
        pingpong.proc.apply(0L).sock.send.apply(1L, initial_msg);

        for (int i = 0; i < 10; i++) {
            pingpong.run();
        }
    }
}