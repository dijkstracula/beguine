package net.dijkstracula.irving.protocols.chainrep;

import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Tee;
import org.junit.jupiter.api.Test;

public class DivergentExecutionTests {
    @Test
    public void linearizableAndLocalChainRepAreDifferent() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        Tee<LinearizableChainRep.ChainRep, LocalReadChainRep.ChainRep> t = new Tee<>(
                ctx,
                new LinearizableChainRep.ChainRep(ctx),
                new LocalReadChainRep.ChainRep(ctx));
        t.tee1("read", t.spec.read, t.impl.read, t.spec.pid.generator());
        t.tee2("append", t.spec.append, t.impl.append, t.spec.pid.generator(), ctx.randomSmallNat());
        t.tee1("sock.recv", t.spec.net.recvf, t.impl.net.recvf, ctx.randomSelect(t.spec.net.sockets.keySet().stream().toList()));

        for (int i = 0; i < 1000; i++) {
            t.run();
        }
    }

    @Test
    public void linearizableAndLocalNetworkReadChainRepAreDifferent() {
        MelinaContext ctx = MelinaContext.fromSeed(42);
        Tee<LinearizableChainRep.ChainRep, LocalNetworkReadChainRep.ChainRep> t = new Tee<>(
                ctx,
                new LinearizableChainRep.ChainRep(ctx),
                new LocalNetworkReadChainRep.ChainRep(ctx));
        t.tee1("read", t.spec.read, t.impl.read, t.spec.pid.generator());
        t.tee2("append", t.spec.append, t.impl.append, t.spec.pid.generator(), ctx.randomSmallNat());
        t.tee1("sock.recv", t.spec.net.recvf, t.impl.net.recvf, ctx.randomSelect(t.spec.net.sockets.keySet().stream().toList()));

        for (int i = 0; i < 1000; i++) {
            t.run();
        }
    }

    @Test
    public void identicalLinearizableChainRepAreTheSame() {
        MelinaContext ctx = MelinaContext.fromSeed(41);
        Tee<LinearizableChainRep.ChainRep, LinearizableChainRep.ChainRep> t = new Tee<>(
                ctx,
                new LinearizableChainRep.ChainRep(ctx),
                new LinearizableChainRep.ChainRep(ctx));
        t.tee1("read", t.spec.read, t.impl.read, t.spec.pid.generator());
        t.tee2("append", t.spec.append, t.impl.append, t.spec.pid.generator(), ctx.randomSmallNat());
        t.tee1("sock.recv", t.spec.net.recvf, t.impl.net.recvf, ctx.randomSelect(t.spec.net.sockets.keySet().stream().toList()));

        for (int i = 0; i < 1000; i++) {
            t.run();
        }
    }
}
