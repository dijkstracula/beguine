package net.dijkstracula.irving.protocols.chainrep;

import io.vavr.Function1;
import net.dijkstracula.irving.sorts.IvyClass;
import net.dijkstracula.irving.sorts.IvySort;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.irving.sorts.UnboundedSequence;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.history.ActionCall;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.collections.Vector;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class LocalReadChainRep {

    public static class ChainRep extends Protocol {
        int read_req = 0;
        int read_resp = 1;
        int append_req = 2;
        int append_resp = 3;

        final ReliableNetwork<Msg> net;

        final Range pid;

        Function1<Long, ChainRep.IvyObj_host> host;

        Action1<Long, Void> read = new Action1<>();
        Action2<Long, Long, Void> append = new Action2<>();

        public ChainRep(MelinaContext ctx) {
            super(ctx);
            net = new ReliableNetwork<>(ctx);
            pid = ctx.mkRange("pid", 0, 3);
            exportAction("net.recvf_1", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf_2", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf_3", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf_4", net.recvf, ctx.randomSelect(net.sockets));


            List<ChainRep.IvyObj_host> host_instances = LongStream.range(0, 3).mapToObj(i -> new ChainRep.IvyObj_host(i)).collect(Collectors.toList());
            host = i -> host_instances.get(i.intValue());

            // Virtual actions for parameterized object
            read.on((self) -> {
                return host.apply(self).read.apply();
            });
            exportAction("read", read, pid.generator());

            append.on((self, val) -> {
                return host.apply(self).append.apply(val);
            });
            exportAction("append", append, pid.generator(), ctx.randomSmallNat());
        }

        class Ivy_msg_t_Factory extends IvyClass<Msg> {
            // Actions


            // Fields
            IvySort<Long, ?> kind;
            IvySort<Long, ?> src;
            IvySort<Long, ?> to_append;
            IvySort<Vector<Long>, ?> read_state;


            public Ivy_msg_t_Factory(MelinaContext ctx) {
                super("msg_t", ctx);
                kind = ctx.mkRange("kind", 0, 4);
                src = pid;
                to_append = new UnboundedSequence(ctx);
                //read_state = vector;
            }

            @Override
            public Msg make() { return new Msg(); }

            @Override
            public Supplier<Msg> generator() {
                Supplier<Long> kind_gen = kind.generator();
                Supplier<Long> src_gen = src.generator();
                Supplier<Long> to_append_gen = to_append.generator();
                Supplier<Vector<Long>> read_state_gen = read_state.generator();
                return () -> {
                    Msg ret = make();
                    ret.kind = kind_gen.get();
                    ret.src = src_gen.get();
                    ret.to_append = to_append_gen.get();
                    ret.read_state = read_state_gen.get();
                    return ret;
                };
            }
        }
        ChainRep.Ivy_msg_t_Factory msg_t_metaclass = new ChainRep.Ivy_msg_t_Factory(ctx);

        class IvyObj_host {

            protected Action0<Void> read = new Action0<>();
            protected Action1<Long, Void> append = new Action1<>();
            private void show(Vector<Long> content) {
                System.out.println("[host %d]: show: " + self + content);
            };

            private Long self;

            private Vector<Long> contents;

            private ReliableNetwork<Msg>.Socket sock;

            public IvyObj_host(Long self) {
                this.self = self;

                this.sock = net.dial.apply(self);

                contents = Vector.empty();
                append.on((Long val) -> {
                    System.out.println(String.format("[host %d] append(%d)", self, val));
                    addHistory(ActionCall.fromAction2("append", self, val));

                    Msg msg = msg_t_metaclass.make();
                    msg.src = ((self) >= 2 ? 2 : ((self) < 0 ? 0 : (self)));
                    msg.kind = append_req;
                    msg.to_append = val;

                    sock.send.apply(host.apply(0L).sock.id, msg);

                    return null;
                });
                read.on(() -> {
                    System.out.println(String.format("[host %d] read()", self));
                    addHistory(ActionCall.fromAction0("read"));

                    show(contents);
                    return null;
                });
                sock.recv.on((Long src, Msg msg) -> {
                    System.out.println(String.format("[host %d] sock.recv(%d, %s)", self, src, msg));
                    addHistory(ActionCall.fromAction3("sock.recv", self, src, msg));

                    if (msg.kind == read_req) {
                        //assert self == 2;
                        Msg resp = msg_t_metaclass.make();
                        resp.kind = read_resp;
                        resp.src = ((self) >= 2 ? 2 : ((self) < 0 ? 0 : (self)));
                        resp.read_state = contents;

                        sock.send.apply(host.apply(((msg.src) >= 2 ? 2 : ((msg.src) < 0 ? 0 : (msg.src)))).sock.id, resp);
                    } else if (msg.kind == read_resp) {
                        show(msg.read_state);
                    } else if (msg.kind == append_req) {
                        contents = contents.append(msg.to_append);
                        System.out.println(String.format("[host %d] contents = %s", self, contents));
                        if (self < 2) {
                            sock.send.apply(host.apply((self + 1) < 0 ? 0 : (self + 1)).sock.id, msg);
                        } else {
                            Msg resp = msg_t_metaclass.make();
                            resp.kind = append_resp;
                            resp.src = ((self) >= 2 ? 2 : ((self) < 0 ? 0 : (self)));
                            resp.read_state = contents;
                            sock.send.apply(host.apply(((msg.src) >= 2 ? 2 : ((msg.src) < 0 ? 0 : (msg.src)))).sock.id, resp);
                        }
                    } else if (msg.kind == append_resp) {
                    } else {
                        assert false;
                    }

                    return null;
                });

                ChainRep.IvyObj_host.IvyObj_spec spec = new ChainRep.IvyObj_host.IvyObj_spec();
            }

            class IvyObj_spec {


                private long msg_count;

                public IvyObj_spec() {
                    msg_count = 0;
                    sock.send.after((Long dest, Msg m, Void v) -> {
                        msg_count = (msg_count + 1) < 0 ? 0 : (msg_count + 1);
                        System.out.println(String.format("[host %d]: after sock.send: msg_count=%d", self, msg_count));
                        return v;
                    });
                    sock.recv.after((Long src, Msg m, Void v) -> {
                        //ensureThat(msg_count > 0);
                        msg_count = (msg_count - 1) < 0 ? 0 : (msg_count - 1);
                        System.out.println(String.format("[host %d]: after sock.recv: msg_count=%d", self, msg_count));
                        return v;
                    });

                } //cstr
            }

        }
    }

    @Test
    public void LinearizableChainRepTest() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        ChainRep p = new ChainRep(ctx);

        for (int i = 0; i < 100; i++) {
            p.run();
        }
    }

}
