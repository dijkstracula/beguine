package net.dijkstracula.irving.protocols;

import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.stdlib.collections.*;
import net.dijkstracula.melina.stdlib.net.*;

/*
public class LinearizableChainReplication {
    class file extends Vector<Long> {
    }

    class net extends tcp.net<Long> {
    }

    class IvyObj_host {
        protected Action1<Long, Void> append = new Action1<>();
        private void show(Vector content) {
            System.out.println("show: " + content);
        };

        private Long self;

        private Vector contents;

        public IvyObj_host(Long self) {
            this.self = self;

            contents = file.empty();
            append.on((Long val) -> {
                sock.send(host(0).sock.id, val);
                return null;
            });
            sock.recv.on((Long src, Long m) -> {
                contents = contents.append(val);
                if (self < 2) {
                    sock.send(host((self + 1) < 0 ? 0 : (self + 1)).sock.id, val)}
                ;
                show(contents);
                return null;
            });

        } //cstr
        class IvyObj_spec {
            private long msg_count;

            public IvyObj_spec() {

                msg_count = 0;
                sock.send.onAfter((Long dest, Long m) -> {
                    msg_count = (msg_count + 1) < 0 ? 0 : (msg_count + 1);
                });
                sock.recv.onAfter((Long src, Long m) -> {
                    msg_count = (msg_count - 1) < 0 ? 0 : (msg_count - 1);
                    ensureThat(IntStream.range(0, 3).flatMap(P1 -> {
                        return IntStream.range(0, 3).allMatch(P2 -> {
                            return !(msg_count == 0) || host(P1).contents.end() == host(P2).contents.end() && IntStream.range(0, host(P1).contents.end()).allMatch(I -> {
                                return !(I < host(P1).contents.end()) || host(P1).contents.get(I) == host(P2).contents.get(I);
                            });
                        });
                    }));
                });

            } //cstr


        }
        IvyObj_spec spec = new IvyObj_spec();
    }
    IvyObj_host host[] = IntStream(0, 3).boxed().map(i -> new IvyObj_host(i)).toArray();
}

 */