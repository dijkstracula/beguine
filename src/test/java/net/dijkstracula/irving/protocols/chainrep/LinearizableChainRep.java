package net.dijkstracula.irving.protocols.chainrep;

import io.vavr.Function1;
import net.dijkstracula.irving.sorts.IvyClass;
import net.dijkstracula.irving.sorts.IvySort;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.irving.sorts.UnboundedSequence;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.collections.Vector;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class LinearizableChainRep {
    public class ChainRep extends Protocol {
        int read_req = 0;
        int read_resp = 1;
        int append_req = 2;
        int append_resp = 3;

        final ReliableNetwork<msg_t> net;

        final Range pid;

        Function1<Long, IvyObj_host> host;

        public ChainRep(MelinaContext ctx) {
            super(ctx);
            net = new ReliableNetwork<>(ctx);
            pid = ctx.mkRange("pid", 0, 3);
            exportAction("net.recvf(1)", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf(2)", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf(3)", net.recvf, ctx.randomSelect(net.sockets));
            exportAction("net.recvf(4)", net.recvf, ctx.randomSelect(net.sockets));


            List<IvyObj_host> host_instances = LongStream.range(0, 3).mapToObj(i -> new IvyObj_host(i)).collect(Collectors.toList());
            host = i -> host_instances.get(i.intValue());

            // Virtual actions for parameterized object
            Action2<Long, Long, Void> append = new Action2<>();
            append.on((self, val) -> {
                return host.apply(self).append.apply(val);
            });
            exportAction("append", append, pid.generator(), ctx.randomSmallNat());
        }

        class msg_t {
            // Fields
            public long kind;
            public long src;
            public long to_append;
            public Vector<Long> read_state;

            // Actions


            @Override
            public String toString() {
                return "msg_t{" +
                        "kind=" + kind +
                        ", src=" + src +
                        ", to_append=" + to_append +
                        ", read_state=" + read_state +
                        '}';
            }
        }
        class Ivy_msg_t_Factory extends IvyClass<msg_t> {
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
            public msg_t make() { return new msg_t(); }

            @Override
            public Supplier<msg_t> generator() {
                Supplier<Long> kind_gen = kind.generator();
                Supplier<Long> src_gen = src.generator();
                Supplier<Long> to_append_gen = to_append.generator();
                Supplier<Vector<Long>> read_state_gen = read_state.generator();
                return () -> {
                    msg_t ret = make();
                    ret.kind = kind_gen.get();
                    ret.src = src_gen.get();
                    ret.to_append = to_append_gen.get();
                    ret.read_state = read_state_gen.get();
                    return ret;
                };
            }
        }
        Ivy_msg_t_Factory msg_t_metaclass = new Ivy_msg_t_Factory(ctx);

        class IvyObj_host {

            protected Action0<Void> read = new Action0<>();
            protected Action1<Long, Void> append = new Action1<>();
            private void show(Vector<Long> content) {
                System.out.println("show: " + content);
            };

            private Long self;

            private Vector<Long> contents;

            private ReliableNetwork<msg_t>.Socket sock;

            public IvyObj_host(Long self) {
                this.self = self;

                this.sock = net.dial.apply(self);

                contents = Vector.empty();
                append.on((Long val) -> {
                    System.out.println(String.format("[host %d] append(%d)", self, val));

                    msg_t msg = msg_t_metaclass.make();
                    msg.src = ((self) >= 2 ? 2 : ((self) < 0 ? 0 : (self)));
                    msg.kind = append_req;
                    msg.to_append = val;
                    sock.send.apply(host.apply(0L).sock.id, msg);
                    return null;
                });
                read.on(() -> {
                    System.out.println(String.format("[host %d] append()", self));

                    msg_t msg = msg_t_metaclass.make();
                    msg.src = ((self) >= 2 ? 2 : ((self) < 0 ? 0 : (self)));
                    msg.kind = read_req;
                    sock.send.apply(host.apply(2L).sock.id, msg);
                    return null;
                });
                sock.recv.on((Long src, msg_t msg) -> {
                    System.out.println(String.format("[host %d] sock.recv(%d, %s)", self, src, msg));

                    if (msg.kind == read_req) {
                        assert self == 2;
                        msg_t resp = msg_t_metaclass.make();
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
                            msg_t resp = msg_t_metaclass.make();
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

                IvyObj_spec spec = new IvyObj_spec();
            }

            class IvyObj_spec {


                private long msg_count;

                public IvyObj_spec() {
                    msg_count = 0;
                    sock.send.after((Long dest, msg_t m, Void v) -> {
                        msg_count = (msg_count + 1) < 0 ? 0 : (msg_count + 1);
                        System.out.println(String.format("[host %d]: after sock.send: msg_count=%d", self, msg_count));
                        return v;
                    });
                    sock.recv.after((Long src, msg_t m, Void v) -> {
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

        for (int i = 0; i < 1000; i++) {
            p.run();
        }
    }

}
