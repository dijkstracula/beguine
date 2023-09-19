package net.dijkstracula.irving.protocols;

import com.microsoft.z3.Context;
import io.vavr.Function2;
import io.vavr.Function3;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.collections.*;
import net.dijkstracula.melina.stdlib.net.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearizableChainReplication {

    public class ChainRep extends Protocol {
        private final Sorts sorts;
        private final Sorts.Range pid;

        final ReliableNetwork<Long> net;

        public ChainRep(Random r) {
            super(r);

            sorts = new Sorts(new Context(), random);
            pid = sorts.mkRange("pid", 0, 2);
            net = new ReliableNetwork<>(r);
            addAction("recvf", net.recvf, net::randomSockId);

            host_instances = IntStream.range(0, 3).boxed().map(j -> new IvyObj_host(j.longValue())).collect(Collectors.toList());
        }

        class IvyObj_host {
            protected Action1<Long, Void> append = new Action1<>();

            private void show(Vector content) {
                System.out.print(String.format("show %d: [", self));

                for (int i = 0; i < content.end(); i++) {
                    if (i > 0) {
                        System.out.print(",");
                    }
                    System.out.print(content.get(i));
                }
                System.out.println("]");
            }

            private Long self;

            private Vector<Long> contents;

            ReliableNetwork.Socket sock;

            public IvyObj_host(Long self) {
                this.self = self;

                sock = net.dial.apply(self);

                contents = Vector.empty();
                append.on((Long val) -> {
                    System.out.println(String.format("[host %d] append %d", self, val));
                    sock.send.apply(host(0).sock.id, val);
                    return null;
                });
                addAction(String.format("append-from-%d", self), append, () -> random.nextLong() % 10); // XXX: generator for "small ints"

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
        Random r = new Random(42);
        ChainRep p = new ChainRep(r);

        for (int i = 0; i < 1000; i++) {
            p.run();
        }
    }

}