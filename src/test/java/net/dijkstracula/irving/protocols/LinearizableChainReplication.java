package net.dijkstracula.irving.protocols;

import com.microsoft.z3.Context;
import io.vavr.Function2;
import io.vavr.Function3;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.collections.*;
import net.dijkstracula.melina.stdlib.net.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearizableChainReplication extends Protocol {
    private final Sorts sorts;
    private final Sorts.Range pid;

    final ReliableNetwork<Long> net;

    public LinearizableChainReplication(Random r) {
        super(r);

        sorts = new Sorts(new Context(), random);
        pid = sorts.mkRange("pid", 0, 2);

        net = new ReliableNetwork<>(r);
    }

    class IvyObj_host {
        protected Action1<Long, Void> append = new Action1<>();
        private void show(Vector content) {
            System.out.println("show: " + content);
        };

        private Long self;

        private Vector<Long> contents;

        ReliableNetwork.Socket sock;

        public IvyObj_host(Long self) {
            this.self = self;

            sock = net.dial.apply(self);

            contents = Vector.empty();
            append.on((Long val) -> {
                sock.send.apply(host(0).sock.id, val);
                return null;
            });
            sock.recv.on((Function2<Long, Long, Void>) (src, msg) -> {
                contents = contents.append(msg);
                if (self < 2) {
                    sock.send.apply(host((int)((self + 1) < 0 ? 0 : (self + 1))).sock.id, msg);
                }
                show(contents);
                return null;
            });
        } //cstr
        class IvyObj_spec {
            private int msg_count;

            public IvyObj_spec() {

                msg_count = 0;
                sock.send.after((Function3<Long, Long, Void, Void>)(Long dest, Long m, Void ret) -> {
                    msg_count = (msg_count + 1) < 0 ? 0 : (msg_count + 1);
                    return null;
                });
                sock.recv.after((Function3<Long, Long, Void, Void>)(Long src, Long m, Void ret) -> {
                    msg_count = (msg_count - 1) < 0 ? 0 : (msg_count - 1);
                    addConjecture("quiescent-net-means-same-state", () -> IntStream.range(0, 3).allMatch(P1 -> {
                        return IntStream.range(0, 3).allMatch(P2 -> {
                            return !(msg_count == 0) || host(P1).contents.end() == host(P2).contents.end() && IntStream.range(0, host(P1).contents.end()).allMatch(I -> {
                                return !(I < host(P1).contents.end()) || host(P1).contents.get(I) == host(P2).contents.get(I);
                            });
                        });
                    }));
                    return null;
                });
            } //cstr
        }
        IvyObj_spec spec = new IvyObj_spec();
    }
    List<IvyObj_host> host_instances = IntStream.range(0, 3).boxed().map(i -> new IvyObj_host(i.longValue())).collect(Collectors.toList());
    public IvyObj_host host(int i) { return host_instances.get(i); }
}