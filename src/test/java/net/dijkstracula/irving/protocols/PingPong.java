package net.dijkstracula.irving.protocols;

import net.dijkstracula.irving.sorts.IvyClass;
import net.dijkstracula.irving.sorts.IvySort;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.irving.sorts.UnboundedSequence;
import net.dijkstracula.melina.actions.Action1;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import java.lang.Void;

public class PingPong extends Protocol {

    final ReliableNetwork<msg_t> net;

    final Range pid;

    List<IvyObj_proc> proc_instances;
    public IvyObj_proc host(int i) {
        return proc_instances.get(i);
    }

    public PingPong(MelinaContext ctx) {
        super(ctx);
        net = new ReliableNetwork<>(ctx);
        exportAction("net.recvf", net.recvf, ctx.randomSelect(net.sockets));

        pid = ctx.mkRange("pid", 0, 3);

        // Instantiate parameterized objects
        proc_instances = new ArrayList<>();
        for (long i = pid.min; i < pid.max; i++){
            proc_instances.add(new IvyObj_proc(i));
        }
    }

    class msg_t {
        // Actions
        public Action0<Void> handle = new Action0<>(() ->
            Ivy_msg_t_Factory.handle.apply(this)
        );

    }
    class Ivy_msg_t_Factory extends IvyClass<msg_t> {
        // Actions
        public static Action1<msg_t, Void> handle = new Action1<>();

        public Ivy_msg_t_Factory(MelinaContext ctx) {
            super("msg_t", ctx);
        }

        @Override
        public msg_t make() { return new msg_t(); }

        @Override
        public Supplier<msg_t> generator() {
            return () -> {
                msg_t ret = make();
                return ret;
            };
        }
    }

    class ping_t {
        // Fields
        public long ping_val;

        // Actions
        public Action0<Void> handle = new Action0<>(() -> Ivy_ping_t_Factory.handle.apply(this));

    }
    class Ivy_ping_t_Factory extends IvyClass<ping_t> {
        // Actions
        public static Action1<ping_t, Void> handle = new Action1<>();


        // Fields
        IvySort<Long, ?> ping_val;


        public Ivy_ping_t_Factory(MelinaContext ctx) {
            super("ping_t", ctx);
            ping_val = new UnboundedSequence(ctx);
        }

        @Override
        public ping_t make() { return new ping_t(); }

        @Override
        public Supplier<ping_t> generator() {
            Supplier<Long> ping_val_gen = ping_val.generator();
            return () -> {
                ping_t ret = make();
                ret.ping_val = ping_val_gen.get();
                return ret;
            };
        }
    }

    class pong_t {
        // Fields
        public long pong_val;

        // Actions
        public Action0<Void> handle = new Action0<>(() -> Ivy_pong_t_Factory.handle.apply(this));

    }
    class Ivy_pong_t_Factory extends IvyClass<pong_t> {
        // Actions
        public static Action1<pong_t, Void> handle = new Action1<>();


        // Fields
        IvySort<Long, ?> pong_val;


        public Ivy_pong_t_Factory(MelinaContext ctx) {
            super("pong_t", ctx);
            pong_val = new UnboundedSequence(ctx);
        }

        @Override
        public pong_t make() { return new pong_t(); }

        @Override
        public Supplier<pong_t> generator() {
            Supplier<Long> pong_val_gen = pong_val.generator();
            return () -> {
                pong_t ret = make();
                ret.pong_val = pong_val_gen.get();
                return ret;
            };
        }
    }

    class IvyObj_proc {
        final Long self;

        final ReliableNetwork<msg_t>.Socket sock;

        public IvyObj_proc(Long self) {
            this.self = self;
            this.sock = net.dial.apply(self);

            sock.recv.on((Long src, msg_t m) -> {
                m.handle.apply();
                return null;
            });

        } //cstr
    }
}