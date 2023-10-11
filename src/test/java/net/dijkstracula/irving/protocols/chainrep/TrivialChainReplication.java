package net.dijkstracula.irving.protocols.chainrep;

import io.vavr.Function2;
import io.vavr.Function3;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.runtime.Tee;
import net.dijkstracula.melina.stdlib.collections.*;
import net.dijkstracula.melina.stdlib.net.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TrivialChainReplication {

    public class ChainRep extends Protocol {
        final ReliableNetwork<Long> net;
        final Range pid;

        // Exported actions
        final Action1<ReliableNetwork<Long>.Socket, Void> recvf;
        Action2<Long, Long, Void> append;

        public ChainRep(MelinaContext ctx) {
            super(ctx);

            // Instantiate types
            pid = ctx.mkRange("pid", 0, 3);

            // Instantiate modules
            net = new ReliableNetwork<>(ctx);
            exportAction("net.recvf", net.recvf, ctx.randomSelect(net.sockets));
            recvf = net.recvf;

            // Instantiate parameterized objects
            host_instances = new ArrayList<>();
            for (long i = pid.min; i < pid.max; i++){
                host_instances.add(new IvyObj_host(i));
            }
            // Virtual actions for parameterized object
            append = new Action2<>();
            append.on((self, val) -> {
                return host_instances.get(self.intValue()).append.apply(val);
            });
            exportAction("append", append, pid.generator(), ctx.randomSmallNat());
        }

        class IvyObj_host {
            public Action1<Long, Void> append = new Action1<>();

            private void show(Vector content) {
                System.out.println(String.format("show %d: %s", self, content));
            }

            private Long self;

            private Vector<Long> contents;

            ReliableNetwork.Socket sock;

            public IvyObj_host(Long self) {
                this.self = self;

                // After init
                sock = net.dial.apply(self);
                contents = Vector.empty();

                append.on((Long val) -> {
                    System.out.println(String.format("[host %d] append %d", self, val));
                    sock.send.apply(host(0).sock.id, val);
                    return null;
                });

                sock.recv.on((Function2<Long, Long, Void>) (src, msg) -> {
                    System.out.println(String.format("[Recv %d] got %d from %d", self, msg, src));
                    contents = contents.append(msg);
                    if (self < 2) {
                        sock.send.apply(host((int) ((self + 1) < 0 ? 0 : (self + 1))).sock.id, msg);
                    }
                    show(contents);
                    return null;
                });

                IvyObj_spec spec = new IvyObj_spec();

            } //cstr

            class IvyObj_spec {
                private int msg_count;

                public IvyObj_spec() {

                    msg_count = 0;
                    sock.send.after((Function3<Long, Long, Void, Void>) (Long dest, Long m, Void ret) -> {
                        msg_count = (msg_count + 1) < 0 ? 0 : (msg_count + 1);
                        return null;
                    });
                    sock.recv.after((Function3<Long, Long, Void, Void>) (Long src, Long m, Void ret) -> {
                        msg_count = (msg_count - 1) < 0 ? 0 : (msg_count - 1);
                        return null;
                    });

                } //cstr
            }

        }

        List<IvyObj_host> host_instances;
        public IvyObj_host host(int i) {
            return host_instances.get(i);
        }
    }

    @Test
    public void LinearizableChainRepTest() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        ChainRep p = new ChainRep(ctx);

        for (int i = 0; i < 1000; i++) {
            p.run();
        }
    }

    @Test
    public void LinearizableChainRepTee() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        ChainRep spec = new ChainRep(ctx);
        ChainRep impl = new ChainRep(ctx);

        Tee<ChainRep, ChainRep> t = new Tee<>(ctx, spec, impl);
        t.tee1("recvf", spec.net.recvf, impl.net.recvf, ctx.randomSelect(spec.net.sockets));
        t.tee2("append", spec.append, impl.append, spec.pid.generator(), ctx.randomSmallNat());

        // The behaviour of the two protocols under test should, of course, be identical.
        for (int i = 0; i < 100; i++) {
            t.run();
        }
    }
}